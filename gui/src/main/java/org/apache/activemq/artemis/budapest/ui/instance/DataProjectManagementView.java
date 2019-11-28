package org.apache.activemq.artemis.budapest.ui.instance;

import org.apache.activemq.artemis.budapest.data.record.DataRecord;
import org.apache.activemq.artemis.budapest.project.AbstractProject;
import org.apache.activemq.artemis.budapest.project.book.StatusInfo;
import org.apache.activemq.artemis.budapest.project.internal.ArtemisDataProject;
import org.apache.activemq.artemis.budapest.ui.CommonUI;
import org.apache.activemq.artemis.budapest.ui.workspace.DataWorkSpace;
import org.apache.activemq.artemis.budapest.ui.workspace.EventMember;
import org.apache.activemq.artemis.budapest.ui.workspace.WorkspaceEvent;
import org.apache.activemq.artemis.budapest.ui.workspace.WorkspaceEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.Enumeration;

public class DataProjectManagementView implements WorkspaceEventListener, EventMember
{
   private static final Logger logger = LoggerFactory.getLogger(DataProjectManagementView.class);
   private static final String LOG_LIST = "log-list";
   private static final String NO_LOG_WARN = "no-log-warn";

   private MainInstance instance;
   private JScrollPane projectPane;
   private JScrollPane logListPane;
   private JPanel logPanel;
   private CardLayout cardLayout;

   private JTree logProjectTree;
   private DefaultTreeModel treeModel;
   private JTree logListTree;
   private AbstractProject current;

   public DataProjectManagementView(MainInstance instance)
   {
      this.instance = instance;
      DataWorkSpace workspace = instance.getWorkspace();

      treeModel = workspace.getWorkspaceTreeModel();
      logProjectTree = new JTree(treeModel);
      LogProjectTreeRenderer renderer = new LogProjectTreeRenderer();
      logProjectTree.setCellRenderer(renderer);

      LogListTreeRenderer logTreeRenderer = new LogListTreeRenderer();
      logListTree = new JTree();
      logListTree.setRootVisible(false);//don't show root node
      logListTree.setCellRenderer(logTreeRenderer);
      logListTree.setModel(ArtemisDataProject.emptyLogTreeModel());

      MouseListener logListMouseListener = new MouseAdapter()
      {
         public void mouseClicked(MouseEvent e)
         {
            int selRow = logListTree.getRowForLocation(e.getX(), e.getY());
            TreePath selPath = logListTree.getPathForLocation(e.getX(), e.getY());
            if (selPath == null) return;

            Object comp = selPath.getLastPathComponent();

            if (e.getButton() == MouseEvent.BUTTON3)
            {
               if(selRow != -1)
               {
                  showPopupMenu(comp, e);
               }
            }
            else if (e.getButton() == MouseEvent.BUTTON1)
            {
               Object data = ((DefaultMutableTreeNode)comp).getUserObject();
               if (data != null)
               {
                  instance.broadcast(new UserEvent(this, UserEvent.UPDATE_STATUS, new StatusInfo(data.toString())));
               }
            }
         }
      };
      logListTree.addMouseListener(logListMouseListener);

      projectPane = new JScrollPane(logProjectTree);
      logListPane = new JScrollPane(logListTree);
      logPanel = new JPanel();
      cardLayout = new CardLayout();
      logPanel.setLayout(cardLayout);
      logPanel.add(logListPane, LOG_LIST);
      JLabel noLog = new JLabel("<Empty>");
      logPanel.add(noLog, NO_LOG_WARN);

      MouseListener ml = new MouseAdapter()
      {
         public void mouseClicked(MouseEvent e)
         {
            int selRow = logProjectTree.getRowForLocation(e.getX(), e.getY());
            TreePath selPath = logProjectTree.getPathForLocation(e.getX(), e.getY());
            if (selPath == null) return;

            Object comp = selPath.getLastPathComponent();

            if (e.getButton() == MouseEvent.BUTTON3)
            {
               if(selRow != -1)
               {
                  showPopupMenu(comp, e);
               }
            }
            else if (e.getButton() == MouseEvent.BUTTON1)
            {
               Object data = ((DefaultMutableTreeNode)comp).getUserObject();
               if (comp instanceof DataWorkSpace.TreeRoot)
               {
                  current = null;
               }
               else if (data instanceof AbstractProject)
               {
                  current = (AbstractProject)data;
                  instance.broadcast(new UserEvent(this, UserEvent.PROJ_SELECTED, data));
                  if (e.getClickCount() > 0)
                  {
                     //double click
                     tryOpenProject(current);
                     logProjectTree.repaint();
                  }
               }
               updateLogList();
            }
         }
      };
      logProjectTree.addMouseListener(ml);

      logProjectTree.addKeyListener(new KeyAdapter() {
         public void keyReleased(KeyEvent ke)
         {
            if (ke.getKeyCode() == KeyEvent.VK_DELETE)
            {
               DefaultMutableTreeNode selected = (DefaultMutableTreeNode) logProjectTree.getLastSelectedPathComponent();
               if (selected != null)
               {
                  Object data = selected.getUserObject();
                  if (data instanceof AbstractProject)
                  {
                     AbstractProject toDel = (AbstractProject)data;
                     if (toDel.isReadOnly()) {
                        return;
                     }
                     try
                     {
                        treeModel.removeNodeFromParent(selected);
                        deleteProject(toDel);
                        if (current == toDel)
                        {
                           current = null;
                           updateLogList();
                        }
                     }
                     catch (Exception e)
                     {
                        logger.error("Failed to delete project {}", selected, e);
                        CommonUI.errorDialog(logProjectTree, "Failed to delete " + selected, e);
                     }
                  }
               }
            }
         }
      });

      workspace.registerListener(this);
      instance.addEventMember(this);
   }

   private void closeProject(ArtemisDataProject proj)
   {
      if (proj.isOpened())
      {
         try
         {
            instance.closeProject(proj);
         }
         catch (Exception ex)
         {
            logger.error("we got an error", ex);
            JOptionPane.showMessageDialog(logProjectTree, "Error open: " + ex.getMessage());
         }
      }
   }

   private void tryOpenProject(AbstractProject proj)
   {
      if (!proj.isOpened())
      {
         try
         {
            instance.openProject(proj);
            if (current != proj)
            {
               current = proj;
               DefaultMutableTreeNode node = findParent((DefaultMutableTreeNode)treeModel.getRoot(), current);
               if (node != null)
               {
                  TreeNode[] nodes = treeModel.getPathToRoot(node);
                  logProjectTree.setSelectionPath(new TreePath(nodes));
                  updateLogList();
               }
            }
         }
         catch (Exception ex)
         {
            logger.error("we got an error", ex);
            JOptionPane.showMessageDialog(logProjectTree, "Error open: " + ex.getMessage());
         }
      }
   }

   public void receive(UserEvent event)
   {
      if (event.getEventType() == UserEvent.ASK_OPEN_PROJ)
      {
         AbstractProject proj = (AbstractProject) event.getEventData();
         instance.getWorkspace().executeBackendTask(new Runnable()
         {
            public void run()
            {
               tryOpenProject(proj);
            }
         });
      }
      else if (event.getEventType() == UserEvent.TO_PARENT)
      {
         //first select the parent
         AbstractProject proj = current.getParent();
         current = proj;
         DefaultMutableTreeNode node = findParent((DefaultMutableTreeNode)treeModel.getRoot(), proj);

         TreeNode[] nodes = treeModel.getPathToRoot(node);
         logProjectTree.setSelectionPath(new TreePath(nodes));
         updateLogList();
         instance.broadcast(new UserEvent(this, UserEvent.PROJ_SELECTED, proj));
         //now goto line
         DataRecord rec = (DataRecord) event.getEventData();
         instance.broadcast(new UserEvent(this, UserEvent.GOTO_PARENT_LINE, rec));
         rec.setLineNumber(rec.getParentLineNumber());
      }
      else if (event.getEventType() == UserEvent.TO_PROJECT)
      {
         AbstractProject proj = (AbstractProject) event.getEventData();
         if (current != proj)
         {
            current = proj;
            DefaultMutableTreeNode node = findParent((DefaultMutableTreeNode)treeModel.getRoot(), proj);

            TreeNode[] nodes = treeModel.getPathToRoot(node);
            logProjectTree.setSelectionPath(new TreePath(nodes));
            updateLogList();
            instance.broadcast(new UserEvent(this, UserEvent.PROJ_SELECTED, proj));
         }
      }
   }

   private void showLogPanel(String name)
   {
      cardLayout.show(logPanel, name);
   }

   private void updateLogList()
   {
      if (current != null)
      {
         DefaultTreeModel listModel = current.getLogListModel();
         logListTree.setModel(listModel);
         listModel.reload();
         if (listModel.getChildCount(listModel.getRoot()) > 0)
         {
            showLogPanel(LOG_LIST);
         }
         else
         {
            showLogPanel(NO_LOG_WARN);
         }
      }
      else
      {
         DefaultTreeModel listModel = (DefaultTreeModel)logListTree.getModel();
         DefaultMutableTreeNode root = (DefaultMutableTreeNode)listModel.getRoot();
         root.removeAllChildren();
         listModel.reload();
      }
   }

   public void handleEvent(WorkspaceEvent event)
   {
      if (event.getEventType() == WorkspaceEvent.NEW_PROJECT)
      {
         DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) treeModel.getRoot();
         AbstractProject newProj = (AbstractProject) event.getEventData();
         AbstractProject parentProj = newProj.getParent();
         DefaultMutableTreeNode newProjNode = new DefaultMutableTreeNode(newProj);

         if (parentProj != null)
         {
            parentNode = findParent(parentNode, parentProj);
         }
         treeModel.insertNodeInto(newProjNode, parentNode, parentNode.getChildCount());

         logProjectTree.scrollPathToVisible(new TreePath(newProjNode.getPath()));
      }
      else if (event.getEventType() == WorkspaceEvent.PROJECT_REMOVED)
      {
         updateLogList();
         instance.broadcast(new UserEvent(this, UserEvent.PROJ_REMOVED, event.getEventData()));
      }
      else if (event.getEventType() == WorkspaceEvent.PROJECT_CLOSED)
      {
         updateLogList();
         instance.broadcast(new UserEvent(this, UserEvent.PROJ_REMOVED, event.getEventData()));
      }
   }

   //method name is bit misleading. it actually search the target project in the tree
   private DefaultMutableTreeNode findParent(DefaultMutableTreeNode baseNode, AbstractProject targetObj)
   {
      DefaultMutableTreeNode result = null;
      Enumeration emrn = baseNode.children();
      while (emrn.hasMoreElements())
      {
         DefaultMutableTreeNode child = (DefaultMutableTreeNode) emrn.nextElement();
         if (child.getUserObject() == targetObj)
         {
            result = child;
            break;
         }
         else
         {
            result = findParent(child, targetObj);
            if (result != null) break;
         }
      }
      return result;
   }

   public JScrollPane getProjectPane()
   {
      return projectPane;
   }

   public JPanel getLogListPane()
   {
      return logPanel;
   }

   private void showPopupMenu(Object comp, MouseEvent e)
   {
      JPopupMenu popupMenu = new JPopupMenu();
      Object eventSource = e.getSource();
      if (comp instanceof DataWorkSpace.TreeRoot)
      {
         JMenuItem newProjItem = new JMenuItem("New Project...");
         newProjItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
               instance.createNewBrokerProject();
            }
         });
         popupMenu.add(newProjItem);
      }
      else if (comp instanceof DefaultMutableTreeNode)
      {
         Object data = ((DefaultMutableTreeNode)comp).getUserObject();
         if (data instanceof AbstractProject)
         {
            AbstractProject selected = (AbstractProject) data;
            if (selected.supportIteration())
            {
               if (!selected.isOpened())
               {
                  JMenuItem openItem = new JMenuItem("Open");
                  openItem.addActionListener(new ActionListener() {
                     public void actionPerformed(ActionEvent e)
                     {
                        tryOpenProject(selected);
                     }
                  });
                  popupMenu.add(openItem);
               }
               else
               {
                  //
               }
            }
         }
      }
      else
      {
         logger.info("===== r-clicked on : " + comp + " for a popup menu. Did you add it?");
      }
      popupMenu.show(e.getComponent(), e.getX(), e.getY());
   }

   private void deleteProject(AbstractProject proj) throws Exception
   {
      logger.info("**** delete project: " + proj);
      proj.delete();
   }

}
