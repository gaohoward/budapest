package org.apache.activemq.artemis.budapest.ui.workspace;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.activemq.artemis.budapest.config.ConfigHelper;
import org.apache.activemq.artemis.budapest.project.AbstractProject;
import org.apache.activemq.artemis.budapest.project.FilterString;
import org.apache.activemq.artemis.budapest.project.bin.BinaryLogProject;
import org.apache.activemq.artemis.budapest.project.internal.ArtemisDataProject;
import org.apache.activemq.artemis.budapest.project.internal.BindingsProject;
import org.apache.activemq.artemis.budapest.project.internal.CoreBindingsProject;
import org.apache.activemq.artemis.budapest.project.internal.JmsBindingsProject;
import org.apache.activemq.artemis.budapest.project.internal.JournalProject;
import org.apache.activemq.artemis.budapest.project.internal.LargeMessageProject;
import org.apache.activemq.artemis.budapest.project.internal.PageLogProject;
import org.apache.activemq.artemis.budapest.project.internal.PagingProject;
import org.apache.activemq.artemis.budapest.project.plain.PlainLogProject;
import org.apache.activemq.artemis.budapest.ui.instance.MainInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

public class DataWorkSpace
{
   private static final Logger logger = LoggerFactory.getLogger(DataWorkSpace.class);

   public static final String TYPE_ARTEMIS = "1";
   public static final String TYPE_JOURNAL = "2";
   public static final String TYPE_BINDINGS = "3";
   public static final String TYPE_PAGING = "4";
   public static final String TYPE_LARGEMESSAGES = "5";
   public static final String TYPE_PLAIN_LOG = "6";
   public static final String TYPE_BINARY_LOG = "7";
   public static final String TYPE_BINDINGS_CORE = "8";
   public static final String TYPE_BINDINGS_JMS = "9";
   public static final String TYPE_PAGE_LOG = "10";

   private MainInstance instance;
   private File baseDir;

   private LinkedHashMap<String, AbstractProject> dataProjects = new LinkedHashMap<>();

   private List<WorkspaceEventListener> listeners = new ArrayList<>();

   private WorkspaceTreeModel treeModel;

   public DataWorkSpace(MainInstance instance, File baseDir) throws IOException
   {
      this.baseDir = baseDir;
      this.instance = instance;

      List<AbstractProject> projects = instance.getConfig().loadProjects(this);
      for (AbstractProject proj : projects)
      {
         dataProjects.put(proj.getID(), proj);
      }
      this.treeModel = new WorkspaceTreeModel(projects);
   }

   public void shutdown()
   {
   }

   public void executeBackendTask(Runnable r)
   {
      instance.getThreadPool().execute(r);
   }

   public void registerListener(WorkspaceEventListener listener)
   {
      listeners.add(listener);
   }

   public void sendStatus(String status)
   {
      instance.sendStatus(status);
   }

   public void eventHappened(WorkspaceEvent event)
   {
      for (WorkspaceEventListener listener : listeners)
      {
         listener.handleEvent(event);
      }
   }

   public boolean isEmpty()
   {
      return dataProjects.size() == 0;
   }

   public void deleteProject(AbstractProject proj) throws Exception
   {
      AbstractProject toRemove = dataProjects.remove(proj.getID());
      if (toRemove != null)
      {
         toRemove.doDelete();
      }
   }

   public File createRawFile(String name) throws IOException
   {
      File rawFile = new File(baseDir, name);
      if (rawFile.exists())
      {
         rawFile.delete();
      }
      rawFile.createNewFile();
      return rawFile;
   }

   public DefaultTreeModel getWorkspaceTreeModel()
   {
      return treeModel;
   }

   public MainInstance getInstance() {
      return instance;
   }

   private class WorkspaceTreeModel extends DefaultTreeModel
   {
      public WorkspaceTreeModel(List<? extends AbstractProject> projects)
      {
         super(new TreeRoot());
         DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) getRoot();
         initModel(projects, parentNode);
      }

      private void initModel(Collection<? extends AbstractProject> projs, DefaultMutableTreeNode parentNode)
      {
         for (AbstractProject prj : projs)
         {
            DefaultMutableTreeNode projNode = new DefaultMutableTreeNode(prj);
            insertNodeInto(projNode, parentNode, parentNode.getChildCount());
            //has children?
            Map<String, AbstractProject> children = prj.getChildren();
            if (children == null) return;
            if (children.size() > 0)
            {
               initModel(children.values(), projNode);
            }
         }
      }
   }

   public class TreeRoot extends DefaultMutableTreeNode
   {
      public TreeRoot()
      {
         super("ProjectRoot");
      }
   }

   public File writeToFile(String fileName, String content) throws IOException
   {
      File file = this.createRawFile(fileName);
      FileWriter writer = new FileWriter(file);
      try
      {
         writer.write(content);
      }
      finally
      {
         writer.close();
      }
      return file;
   }

   public String readRawFile(String fileName) throws IOException
   {
      File file = this.getRawFile(fileName);
      FileReader reader = new FileReader(file);
      char[] buffer = new char[1024];
      StringBuilder builder = new StringBuilder("");
      try
      {
         int n = reader.read(buffer);
         while (n != -1)
         {
            if (n > 0)
            {
               for (int i = 0; i < n; i++)
               {
                  builder.append(buffer[i]);
               }
            }
            n = reader.read(buffer);
         }
      }
      finally
      {
         reader.close();
      }
      return builder.toString();
   }

   private File getRawFile(String fileName) throws FileNotFoundException
   {
      File rawFile = new File(baseDir, fileName);
      if (!rawFile.exists())
      {
         throw new FileNotFoundException("no file " + fileName + " in " + baseDir);
      }
      return rawFile;
   }

   public File getBaseDir()
   {
      return baseDir;
   }

   private void afterCreation(AbstractProject proj)
   {
      dataProjects.put(proj.getID(), proj);
      eventHappened(new WorkspaceEvent(WorkspaceEvent.NEW_PROJECT, proj));
   }

   public JournalProject createJournalProject(ArtemisDataProject parent, File file)
   {
      JournalProject proj = new JournalProject(parent, this, file, null);
      afterCreation(proj);
      return proj;
   }

   //create a new data project
   public ArtemisDataProject createArtemisDataProject(String projName, File baseDir, boolean isArtemisInstance)
   {
      ArtemisDataProject proj = new ArtemisDataProject(projName, this, baseDir, isArtemisInstance);
      afterCreation(proj);
      proj.init();
      return proj;
   }

   public BindingsProject createBindingsProject(ArtemisDataProject parent, File file)
   {
      BindingsProject proj = new BindingsProject(parent, this, file);
      afterCreation(proj);
      return proj;
   }

   public PagingProject createPagingProject(ArtemisDataProject parent, File file)
   {
      PagingProject proj = new PagingProject(parent, this, file);
      afterCreation(proj);
      return proj;
   }

   public LargeMessageProject createLargeMessageProject(ArtemisDataProject parent, File file)
   {
      LargeMessageProject proj = new LargeMessageProject(parent, this, file);
      afterCreation(proj);
      return proj;
   }

   //loading from config
   public AbstractProject loadProject(String projectType, String projID, String projectName, Element projectElement)
   {
      AbstractProject proj;
      if (TYPE_ARTEMIS.equals(projectType))
      {
         String isArtemis = projectElement.getAttribute(ConfigHelper.KEY_ISARTEMISINSTANCE);
         boolean val = isArtemis == null ? true : Boolean.valueOf(isArtemis);
         proj = new ArtemisDataProject(projectName, this, new File(projectElement.getAttribute(ConfigHelper.KEY_BASEDIR)), val);
      }
      else if (TYPE_JOURNAL.equals(projectType))
      {
         //ignore proj name which is fixed
         proj = new JournalProject(null, this, new File(projectElement.getAttribute(ConfigHelper.KEY_BASEDIR)), projID);
      }
      else if (TYPE_BINDINGS.equals(projectType))
      {
         proj = new BindingsProject(null, this, new File(projectElement.getAttribute(ConfigHelper.KEY_BASEDIR)));
      }
      else if (TYPE_PAGING.equals(projectType))
      {
         proj = new PagingProject(null, this, new File(projectElement.getAttribute(ConfigHelper.KEY_BASEDIR)));
      }
      else if (TYPE_LARGEMESSAGES.equals(projectType))
      {
         proj = new LargeMessageProject(null, this, new File(projectElement.getAttribute(ConfigHelper.KEY_BASEDIR)));
      }
      else if (TYPE_PLAIN_LOG.equals(projectType))
      {
         proj = new PlainLogProject(null, projectName, this, projID, Integer.valueOf(projectElement.getAttribute(ConfigHelper.KEY_FILTER_TYPE)),
                  projectElement.getAttribute(ConfigHelper.KEY_FILTER_VALUE));
      }
      else if (TYPE_BINARY_LOG.equals(projectType))
      {
         proj = new BinaryLogProject(null, projectName, this, projID, new File(projectElement.getAttribute(ConfigHelper.KEY_FILE)));
      }
      else if (TYPE_BINDINGS_CORE.equals(projectType))
      {
         proj = new CoreBindingsProject(null, this, projID, new File(projectElement.getAttribute(ConfigHelper.KEY_BASEDIR)));
      }
      else if (TYPE_BINDINGS_JMS.equals(projectType))
      {
         proj = new JmsBindingsProject(null, this, projID, new File(projectElement.getAttribute(ConfigHelper.KEY_BASEDIR)));
      }
      else if (TYPE_PAGE_LOG.equals(projectType))
      {
         proj = new PageLogProject(null, projectName, this, projID, new File(projectElement.getAttribute(ConfigHelper.KEY_BASEDIR)));
      }
      else
      {
         throw new RuntimeException("Unknown project type: " + projectType);
      }
      proj.setID(projID); //redundant??
      return proj;
   }

   public PlainLogProject createPlainLogProject(AbstractProject parent, FilterString filter)
   {
      PlainLogProject proj = new PlainLogProject(parent, this, filter, null);
      afterCreation(proj);
      return proj;
   }
   public BinaryLogProject createBinaryLogProject(AbstractProject parent, File file)
   {
      BinaryLogProject proj = new BinaryLogProject(parent, this, file);
      afterCreation(proj);
      return proj;
   }

   public CoreBindingsProject createCoreBindingsProject(BindingsProject parent, File file)
   {
      CoreBindingsProject proj = new CoreBindingsProject(parent, this, file);
      afterCreation(proj);
      return proj;
   }

   public JmsBindingsProject createJmsBindingsProject(BindingsProject parent, File file)
   {
      JmsBindingsProject proj = new JmsBindingsProject(parent, this, file);
      afterCreation(proj);
      return proj;
   }

   public PageLogProject createPageLogProject(PagingProject parent, File file)
   {
      PageLogProject proj = new PageLogProject(parent, this, file);
      afterCreation(proj);
      return proj;
   }
}
