package org.apache.activemq.artemis.budapest.ui.instance;

import org.apache.activemq.artemis.budapest.data.record.DataRecord;
import org.apache.activemq.artemis.budapest.project.AbstractProject;
import org.apache.activemq.artemis.budapest.project.FilterString;
import org.apache.activemq.artemis.budapest.project.plain.PlainLogProject;
import org.apache.activemq.artemis.budapest.ui.CommonUI;
import org.apache.activemq.artemis.budapest.ui.LogExplorer;
import org.apache.activemq.artemis.budapest.ui.workspace.EventMember;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.text.BadLocationException;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataExplorationView implements EventMember
{
   private static final Logger logger = LoggerFactory.getLogger(DataExplorationView.class);
   private static final String EMPTY_CARD = "empty card";
   private static final String HELP_CARD = "help card";
   private MainInstance instance;
   private JPanel panel;
   private JToolBar controlBar;
   private JPanel projPanel;
   private CardLayout projLayout;

   private Map<String, LogExplorer> liveProjects = new HashMap<>();
   private AbstractProject current;
   private QueryField queryField;
   private JButton gotoPage;

   private JDataDetailPane logDetailPane;

   public DataExplorationView(MainInstance instance)
   {
      this.instance = instance;

      panel = new JPanel();
      controlBar = new JToolBar();
      initToolbar();

      projPanel = new JPanel();
      projLayout = new CardLayout();
      projPanel.setLayout(projLayout);
      JPanel empty = new JPanel();
      JScrollPane help = createHelpPanel();
      projPanel.add(empty, EMPTY_CARD);
      projPanel.add(help, HELP_CARD);

      panel.setLayout(new BorderLayout());
      panel.add(BorderLayout.NORTH, controlBar);
      panel.add(BorderLayout.CENTER, projPanel);

      logDetailPane = new JDataDetailPane(this);
      instance.addEventMember(this);
   }

   private JScrollPane createHelpPanel()
   {
      JScrollPane panel = null;
      try
      {
         JEditorPane helpPane = new JEditorPane();
         helpPane.setEditable(false);
         helpPane.setContentType("text/html");
         helpPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);

         URL helpUrl = instance.getHelpUrl();
         helpPane.setPage(helpUrl);
         panel = new JScrollPane(helpPane);
      }
      catch (Exception e)
      {
         logger.error("Exception loading help", e);
      }
      return panel;
   }

   private void initToolbar()
   {
      queryField = new QueryField();
      JButton locateNext = new JButton("->");
      locateNext.setToolTipText("Locate next...");
      locateNext.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e)
         {
            locateLog(true);
         }
      });
      JButton locatePrev = new JButton("<-");
      locatePrev.setToolTipText("Locate last...");
      locatePrev.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e)
         {
            locateLog(false);
         }
      });

      JButton runQuery = new JButton("R");
      runQuery.setToolTipText("Run query...");
      runQuery.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e)
         {
            doQuery();
         }
      });

      JButton runRegEx = new JButton("X");
      runRegEx.setToolTipText("Regular expression query...");
      runRegEx.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e)
         {
            doRegExQuery();
         }
      });

      JButton backPage = new JButton("<");
      backPage.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e)
         {
            goPrevPage();
         }
      });
      JButton forthPage = new JButton(">");
      forthPage.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e)
         {
            goNextPage();
         }
      });
      gotoPage = new JButton("G");
      gotoPage.setToolTipText("goto line");
      gotoPage.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e)
         {
            gotoLine();
         }
      });

      controlBar.add(queryField.getControl());
      controlBar.add(locatePrev);
      controlBar.add(locateNext);
      controlBar.add(runQuery);
      controlBar.add(runRegEx);
      controlBar.add(backPage);
      controlBar.add(forthPage);
      controlBar.add(gotoPage);
   }

   private void gotoLine()
   {
      String lineNumber = JOptionPane.showInputDialog(panel, "Please give the line number:");
      if (lineNumber != null && (!lineNumber.trim().isEmpty()))
      {
         try
         {
            int ln = Integer.valueOf(lineNumber.trim());
            if (current != null)
            {
               LogExplorer explorer = liveProjects.get(current.getID());
               explorer.gotoLine(ln);
               updatePageNumber();
            }
         }
         catch (Exception e)
         {
            CommonUI.errorDialog(panel, "Error locating line", e);
         }
      }
   }

   public void queryProject(String keyword)
   {
      if (!keyword.isEmpty())
      {
         if (current == null)
         {
            JOptionPane.showMessageDialog(panel, "Please open a project before querying");
            return;
         }
         try
         {
            AbstractProject proj = current.createQueryLog(FilterString.SIMPLE_EX, keyword, liveProjects.get(current.getID()));
            instance.broadcast(new UserEvent(this, UserEvent.ASK_OPEN_PROJ, proj));
            queryField.addHistory(keyword);
         }
         catch (Throwable t)
         {
            logger.error("failed to query project", t);
            JOptionPane.showMessageDialog(panel, "Exception: " + t);
         }
      }
   }

   private void doQuery()
   {
      queryProject(queryField.getText().trim());
   }

   private void doRegExQuery()
   {
      if (current == null)
      {
         JOptionPane.showMessageDialog(panel, "Please open a project before querying");
         return;
      }
      String expression = queryField.getText().trim();
      if (!expression.isEmpty())
      {
         try
         {
            AbstractProject proj = current.createQueryLog(FilterString.REG_EX, expression, liveProjects.get(current.getID()));
            instance.broadcast(new UserEvent(this, UserEvent.ASK_OPEN_PROJ, proj));
         }
         catch (Throwable t)
         {
            logger.error("Failed to do regex query", t);
            JOptionPane.showMessageDialog(panel, "Exception: " + t);
         }
      }
   }

   private void locateLog(boolean forward)
   {
      try
      {
         String keyword = queryField.getText().trim();
         if (current != null)
         {
            LogExplorer explorer = liveProjects.get(current.getID());
            if (!keyword.isEmpty())
            {
               long index = explorer.locateEntry(keyword, forward);
               if (index != -1)
               {
                  updatePageNumber();
               }
            }
            else
            {
               explorer.clearHighlight();
            }
         }
      }
      catch (Exception e)
      {
         logger.error("Error locating log", e);
         CommonUI.errorDialog(queryField.getControl(), "Error locating log", e);
      }
   }

   private void goPrevPage()
   {
      try
      {
         if (current != null)
         {
            LogExplorer explorer = liveProjects.get(current.getID());
            explorer.prevPage();
            updatePageNumber();
         }
      }
      catch (IOException e)
      {
         logger.error("Error doing prev page", e);
         CommonUI.errorDialog(queryField.getControl(), "Error paging backward", e);
      }
   }

   private void goNextPage()
   {
      try
      {
         if (current != null)
         {
            LogExplorer explorer = liveProjects.get(current.getID());
            explorer.nextPage();
            updatePageNumber();
         }
      }
      catch (IOException e)
      {
         logger.error("Error doing next page", e);
         CommonUI.errorDialog(queryField.getControl(), "Error paging forward", e);
      }
   }

   private void updatePageNumber()
   {
      if (current != null)
      {
         LogExplorer explorer = liveProjects.get(current.getID());
         explorer.updatePageInfo();
      }
   }

   public JPanel getLogPane()
   {
      return panel;
   }

   public JDataDetailPane getLogDetailPane()
   {
      return logDetailPane;
   }

   public void closeProject(AbstractProject proj) throws Exception
   {
      if (current != null && current.getID().equals(proj.getID()))
      {
         LogExplorer explorer = liveProjects.remove(proj.getID());
         if (explorer != null)
         {
            projPanel.remove(explorer.getRoot());
            projLayout.show(projPanel, EMPTY_CARD);
            proj.close();
         }
         current = null;
      }
   }

   public void openProject(AbstractProject proj) throws Exception
   {
      if (current != null && current.getID().equals(proj.getID()))
      {
         return;
      }
      LogExplorer explorer = liveProjects.get(proj.getID());
      if (explorer == null)
      {
         int pageSize = instance.getIntConfig("log-page-size", 2048);
         int fontSize = instance.getIntConfig("log-page-font-size", 16);
         explorer = new LogExplorer(instance, proj, pageSize, fontSize, this);
         liveProjects.put(proj.getID(), explorer);
         projPanel.add(explorer.getRoot(), proj.getID());
         updatePageNumber();
      }
      updateProject(proj);
   }

   public void showHelpPanel()
   {
      projLayout.show(projPanel, HELP_CARD);
      current = null;
   }

   public void updateProject(AbstractProject proj) throws Exception
   {
      if (current != null && current.getID().equals(proj.getID()))
      {
         return;
      }

      if (!liveProjects.containsKey(proj.getID()))
      {
         projLayout.show(projPanel, EMPTY_CARD);
         queryField.setText("");
         current = null;
      }
      else
      {
         current = proj;
         if (proj instanceof PlainLogProject)
         {
            FilterString filter = ((PlainLogProject)proj).getFilter();
            queryField.setText(filter == null? "" : filter.toString());
         }
         projLayout.show(projPanel, current.getID());
         updatePageNumber();
      }
      panel.revalidate();
   }

   public void receive(UserEvent event)
   {
      if (event.getEventType() == UserEvent.PROJ_SELECTED)
      {
         AbstractProject proj = (AbstractProject)event.getEventData();
         try
         {
            updateProject(proj);
            if (current != null)
            {
               LogExplorer explorer = this.liveProjects.get(current.getID());
               InfoConsole infoConsole = this.instance.getInfoConsole();
               infoConsole.showInfo(explorer.getLogSummary());
            }
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
      }
      else if (event.getEventType() == UserEvent.PROJ_REMOVED)
      {
         AbstractProject proj = (AbstractProject) event.getEventData();
         liveProjects.remove(proj.getID());
         if (current == proj)
         {
            current = null;
            projLayout.show(projPanel, EMPTY_CARD);
         }
      }
      else if (event.getEventType() == UserEvent.GOTO_PARENT_LINE)
      {
         try
         {
            if (current != null)
            {
               DataRecord rec = (DataRecord) event.getEventData();
               long lineToGo = rec.getParentLineNumber();
               LogExplorer explorer = liveProjects.get(current.getID());
               explorer.gotoLine(lineToGo);
               updatePageNumber();
            }
            else
            {
               logger.warn("no project to locate line");
               CommonUI.errorDialog(panel, "failed to find line. Make sure project is open", null);
            }
         }
         catch (Exception e)
         {
            logger.error("error processing goto line event", e);
            CommonUI.errorDialog(panel, "error goto line", e);
         }
      }
      else if (event.getEventType() == UserEvent.GOTO_LINE)
      {
         try
         {
            if (current != null)
            {
               DataRecord rec = (DataRecord) event.getEventData();
               long lineToGo = rec.getLineNumber();
               if (lineToGo != -1)
               {
                  LogExplorer explorer = liveProjects.get(current.getID());
                  explorer.gotoLine(lineToGo);
                  updatePageNumber();
               }
            }
            else
            {
               logger.warn("no project to locate line");
               CommonUI.errorDialog(panel, "failed to find line. Make sure project is open", null);
            }
         }
         catch (Exception e)
         {
            logger.error("error processing goto line event", e);
            CommonUI.errorDialog(panel, "error goto line", e);
         }
      }
      else if (event.getEventType() == UserEvent.REC_SELECTED)
      {
         List<DataRecord> items = (List<DataRecord>) event.getEventData();
         try
         {
            logDetailPane.updateContent(items.get(items.size()-1).getContent() + " > "+ (items.get(items.size()-1).getPosition()));
         }
         catch (BadLocationException e)
         {
            logger.error("error", e);
            CommonUI.errorDialog(this.logDetailPane, "error happened", e);
            return;
         }
      }
   }

   public MainInstance getInstance()
   {
      return this.instance;
   }

   public AbstractProject getCurrent()
   {
      return this.current;
   }
}
