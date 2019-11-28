package org.apache.activemq.artemis.budapest.project.plain;

import org.apache.activemq.artemis.budapest.config.ConfigHelper;
import org.apache.activemq.artemis.budapest.data.record.DataRecord;
import org.apache.activemq.artemis.budapest.data.store.LogFile;
import org.apache.activemq.artemis.budapest.data.store.LogStore;
import org.apache.activemq.artemis.budapest.filter.LogFilter;
import org.apache.activemq.artemis.budapest.filter.LogFilterImpl;
import org.apache.activemq.artemis.budapest.iterator.JournalIterator;
import org.apache.activemq.artemis.budapest.project.AbstractProject;
import org.apache.activemq.artemis.budapest.project.FilterString;
import org.apache.activemq.artemis.budapest.project.book.LineLocation;
import org.apache.activemq.artemis.budapest.ui.LogExplorer;
import org.apache.activemq.artemis.budapest.ui.workspace.DataWorkSpace;
import org.apache.activemq.artemis.budapest.ui.workspace.WorkspaceEvent;
import org.apache.activemq.artemis.budapest.util.GeneralUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlainLogProject extends AbstractProject
{
   private static final Logger logger = LoggerFactory.getLogger(PlainLogProject.class);

   private FilterString filter;
   private LogFile logFile;
   private ProjectState state;

   public PlainLogProject(AbstractProject parent, DataWorkSpace workspace, FilterString filter, String id)
   {
      super(parent, filter.getName(), workspace, id == null ? GeneralUtil.getTimeID() : id, false);
      this.filter = filter;
      this.state = new ProjectState();
      File file = new File(workspace.getBaseDir(), getID());
      if (file.exists())
      {
         try
         {
            this.addLogFile(file);
            this.state.ready();
         }
         catch (IOException e)
         {
         }
      }
   }

   public PlainLogProject(AbstractProject parent, String projectName, DataWorkSpace workspace, String id, int filterType, String ex)
   {
      this(parent, workspace, new FilterString(ex, filterType), id);
   }

   public void open() throws Exception
   {
      this.state.waitForReady();
      super.open();
   }

   public void writeAttrs(Element projectElem)
   {
      super.writeAttrs(projectElem);
      projectElem.setAttribute(ConfigHelper.KEY_FILTER_TYPE, String.valueOf(filter.filterType));
      projectElem.setAttribute(ConfigHelper.KEY_FILTER_VALUE, filter.ex);
   }

   public String stripLineNo(String rec)
   {
      String result = null;
      int index = rec.indexOf("]");
      result = rec.substring(index+1);
      return result;
   }

   //only a sub project can do filtering
   public void doFiltering(LogExplorer parentExplorer)
   {
      workspace.executeBackendTask(new Runnable() {
         public void run()
         {
            long begin = System.currentTimeMillis();
            boolean isOK = true;
            PrintWriter out = null;
            try
            {
               File rawFile = workspace.createRawFile(getID());
               out = new PrintWriter(new BufferedWriter(new FileWriter(rawFile)));
               JournalIterator<? extends DataRecord> iter = parent.iterator();
               int counter = 0;

               logger.info("got parent iter : " + iter);
               if (filter.filterType == FilterString.REG_EX)
               {
                  Pattern pattern = Pattern.compile(filter.ex);
                  while (iter.hasNext())
                  {
                     DataRecord record = iter.next();
                     counter++;
                     String rec = record.getContent();
                     Matcher matcher = pattern.matcher(rec);
                     if (matcher.find())
                     {
                        out.println("[" + counter + "]" + record.getContent());
                     }
                     if (counter % 5000 == 0)
                     {
                        workspace.sendStatus(projectName + " Processed " + counter + " lines...");
                     }
                  }
               }
               else if (filter.filterType == FilterString.SIMPLE_EX)
               {
                  LogFilterImpl filterImpl = new LogFilterImpl(filter.ex);
                  if (filterImpl.getMode() == LogFilter.CMD.CUT_LINE)
                  {
                     //optimize, not use filter
                     long[] result = filterImpl.parseStartStop();
                     long start = result[0];
                     long stop = result[1];

                     LineLocation loc = parentExplorer.getBookmarkAtLine(start);

                     if (loc != null)
                     {
                        iter.reset(loc.bm);
                        long moves = loc.linesToMove + loc.index;
                        for (int i = 0; i < moves; i++)
                        {
                           iter.next();
                        }
                     }
                     //now iter should stay at the start
                     while (iter.hasNext() && start <= stop)
                     {
                        DataRecord record = iter.next();
                        String content = record.getContent();
                        if (parent != null && (parent instanceof PlainLogProject) && ((PlainLogProject)parent).getFilter() != null)
                        {
                           content = stripLineNo(content);
                        }
                        out.println("[" + start + "]" + content);
                        start++;
                     }
                  }
                  else
                  {
                     int result = LogFilter.NOT_MATCH;
                     while (iter.hasNext())
                     {
                        DataRecord record = iter.next();
                        counter++;

                        try
                        {
                           result = filterImpl.doMatch(record.getContent(), counter);
                        }
                        catch (Exception t)
                        {
                           result = LogFilter.OUT_OF_SCOPE;//stop filtering immediately
                           out.println("Filtering stopped due to following error: " + t.getMessage());
                           t.printStackTrace(out);
                        }

                        if (result == LogFilter.OUT_OF_SCOPE)
                        {
                           break;
                        }
                        if (result == LogFilter.MATCH)
                        {
                           String rec = record.getContent();
                           if (parent != null && (parent instanceof PlainLogProject) && ((PlainLogProject)parent).getFilter() != null)
                           {
                              rec = stripLineNo(rec);
                           }
                           out.println("[" + counter + "]" + rec);
                        }
                        if (counter % 1000 == 0)
                        {
                           workspace.sendStatus(projectName + " Processed " + counter + " lines...");
                        }
                     }
                  }
               }
               out.flush();
               out.close();
               out = null;
               addLogFile(rawFile);
               state.ready();
               long dur = (System.currentTimeMillis() - begin) / 1000;
               workspace.sendStatus("Processing finished " + (isOK ? "successfully." : "with error.") + "Time(sec): " + dur);
            }
            catch (Throwable t)
            {
               logger.error("we got error", t);
            }
            finally
            {
               if (out != null)
               {
                  out.flush();
                  out.close();
               }
            }
         }
      });
   }

   public FilterString getFilter()
   {
      return this.filter;
   }

   private void checkLogFileChange()
   {
      if (opened) throw new IllegalStateException("Project already opened.");
      if (parent != null)
      {
         if (logFile != null)
         {
            throw new IllegalStateException("logFile already exists: " + logFile);
         }
      }
   }

   public void addLogFile(File newFile) throws IOException
   {
      checkLogFileChange();
      logFile = new LogFile(newFile);
      WorkspaceEvent e = new WorkspaceEvent(WorkspaceEvent.NEW_LOG_ADDED, this);
      workspace.eventHappened(e);
   }

   @Override
   public JournalIterator<? extends DataRecord> iterator() throws IOException
   {
      if (!opened) throw new IllegalStateException("Project not opened yet.");
      LogStore logStore = new LogStore();
      logStore.addLogFile(logFile);
      return new JournalIterator<DataRecord>(logStore, 2048);
   }

   private class ProjectState
   {
      private static final int UNINIT = 0;
      private static final int READY = 2;

      private int state;

      public ProjectState()
      {
         this.state = UNINIT;
      }

      public synchronized void waitForReady()
      {
         logger.debug("waiting for ready: " + state + " parent " + parent);
         if (parent == null)
         {
            if (logFile == null)
            {
               throw new IllegalStateException("no log file");
            }
            state = READY;
            return;
         }
         if (logFile != null)
         {
            //children files ready (loaded from config)
            state = READY;
            return;
         }
         while (state != READY)
         {
            try
            {
               this.wait(200);
            }
            catch (InterruptedException e)
            {
               break;
            }
         }
      }

      public synchronized void ready()
      {
         state = READY;
         this.notifyAll();
      }
   }

   @Override
   public String getType()
   {
      return DataWorkSpace.TYPE_PLAIN_LOG;
   }

   public DefaultTreeModel getLogListModel()
   {
      DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode)logTreeModel.getRoot();
      rootNode.removeAllChildren();
      DefaultMutableTreeNode child = new DefaultMutableTreeNode(logFile);
      logTreeModel.insertNodeInto(child, rootNode, rootNode.getChildCount());
      return logTreeModel;
   }

   @Override
   public void doDelete() throws Exception
   {
      super.doDelete();
      if (this.logFile != null)
      {
         this.logFile.delete();
      }
   }

}
