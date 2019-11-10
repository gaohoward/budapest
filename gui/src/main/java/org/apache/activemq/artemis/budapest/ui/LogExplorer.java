package org.apache.activemq.artemis.budapest.ui;

import org.apache.activemq.artemis.budapest.data.record.DataRecord;
import org.apache.activemq.artemis.budapest.project.AbstractProject;
import org.apache.activemq.artemis.budapest.project.book.LineLocation;
import org.apache.activemq.artemis.budapest.project.book.PageOutOfScopeException;
import org.apache.activemq.artemis.budapest.ui.instance.DataExplorationView;
import org.apache.activemq.artemis.budapest.ui.instance.DataProjectModel;
import org.apache.activemq.artemis.budapest.ui.instance.KeywordDecorator;
import org.apache.activemq.artemis.budapest.ui.instance.MainInstance;
import org.apache.activemq.artemis.budapest.ui.instance.RichLogEntryRenderer;
import org.apache.activemq.artemis.budapest.ui.instance.UserEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.util.List;

public class LogExplorer implements ClipboardOwner
{
   private static final Logger logger = LoggerFactory.getLogger(LogExplorer.class);
   private MainInstance instance;
   private JList<DataRecord> pageView;
   private JScrollPane scrollPane;
   private DataProjectModel projectModel;
   private RichLogEntryRenderer logRenderer;
   private KeywordDecorator decorator;
   private DataExplorationView owner;
   private String[] logSummary = new String[3];

   public LogExplorer(MainInstance instance, AbstractProject proj, int pageSize, int fontSize, DataExplorationView owner) throws Exception
   {
      this.instance = instance;
      projectModel = new DataProjectModel(pageSize, instance.getThreadPool());
      pageView = new JList<>();

      pageView.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
      logRenderer = new RichLogEntryRenderer(this, fontSize, projectModel);
      pageView.setCellRenderer(logRenderer);
      projectModel.reset(proj);
      logSummary[0] = "Project Summary";
      logSummary[1] = "Project ID: " + proj.getID();
      pageView.setModel(projectModel);
      scrollPane = new JScrollPane(pageView);
      decorator = new KeywordDecorator();

      MouseListener ml = new MouseAdapter()
      {
         public void mouseClicked(MouseEvent e)
         {
            //int index = list.locationToIndex(e.getPoint());
            if (e.getButton() == MouseEvent.BUTTON3)
            {
               showPopupMenu(e);
            }
            else if (e.getButton() == MouseEvent.BUTTON1)
            {
               if ((e.getModifiersEx() & InputEvent.ALT_DOWN_MASK) == InputEvent.ALT_DOWN_MASK)
               {
                  return;
               }
               //handleSelectionUpdate();
            }
         }
      };
      pageView.addMouseListener(ml);
      pageView.addListSelectionListener(new ListSelectionListener() {

         @Override
         public void valueChanged(ListSelectionEvent e)
         {
            handleSelectionUpdate();
         }

      });

      pageView.addMouseMotionListener(new MouseMotionListener() {
         @Override
         public void mouseMoved(MouseEvent e)
         {
            int index = pageView.locationToIndex(e.getPoint());
            //decorator.setCurrentMouseIndex(index);
            //decorator.setCurrentMousePoint(e.getPoing());
            //final Rectangle cellBounds = pageView.getCellBounds(0, pageView.getModel().getSize() - 1);
            if (index != -1)
            {
               if ((e.getModifiersEx() & InputEvent.ALT_DOWN_MASK) == InputEvent.ALT_DOWN_MASK)
               {
                  pageView.setCursor(new Cursor(Cursor.HAND_CURSOR));
               }
               else
               {
                  pageView.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
               }
            }
            else
            {
               pageView.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
         }

         @Override
         public void mouseDragged(MouseEvent e)
         {
         }
      });
      this.owner = owner;
   }

   private void handleSelectionUpdate()
   {
      List<DataRecord> selectedItems = pageView.getSelectedValuesList();
      if (selectedItems.size() > 0)
      {
         int index = pageView.getAnchorSelectionIndex();
         if (index != -1)
         {
            projectModel.setSelection(index);
            long ln = projectModel.getLineNumber(index);
            UserEvent event = new UserEvent(pageView, UserEvent.REC_SELECTED, selectedItems);
            instance.broadcast(event);
            event = new UserEvent(pageView, UserEvent.UPDATE_STATUS, projectModel.getPageInfo());
            instance.broadcast(event);
         }
      }
   }

   private void showPopupMenu(MouseEvent e)
   {
      JPopupMenu popupMenu = new JPopupMenu();
      List<DataRecord> selectedItems = pageView.getSelectedValuesList();

      if (selectedItems.size() > 0)
      {
         JMenuItem newProjItem = new JMenuItem("Copy...");
         newProjItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
               StringBuilder sb = new StringBuilder();
               for (DataRecord r : selectedItems)
               {
                  sb.append(r.toString());
                  sb.append("\n");
               }
               StringSelection stringSelection = new StringSelection(sb.toString());
               Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
               clipboard.setContents(stringSelection, LogExplorer.this);
            }
         });
         popupMenu.add(newProjItem);

         if (!projectModel.isRootProject())
         {
            JMenuItem toParentItem = new JMenuItem("Location in parent...");
            toParentItem.addActionListener(new ActionListener() {
               public void actionPerformed(ActionEvent e)
               {
                  int index = pageView.getAnchorSelectionIndex();
                  logger.info("--- locating to parent: " + index);
                  if (index != -1)
                  {
                     DataRecord rec = (DataRecord)pageView.getModel().getElementAt(index);
                     logger.info("target rec: " + rec.getContent());
                     UserEvent event = new UserEvent(pageView, UserEvent.TO_PARENT, rec);
                     instance.broadcast(event);
                  }
               }
            });
            popupMenu.add(toParentItem);
         }

         popupMenu.show(e.getComponent(), e.getX(), e.getY());
      }
   }

   public Component getRoot()
   {
      return scrollPane;
   }

   public KeywordDecorator getDecorator()
   {
      return decorator;
   }

   public void clearHighlight()
   {
      decorator.setKeyword("");
      pageView.revalidate();
      pageView.repaint();
   }

   public long locateEntry(String key, boolean forward) throws Exception
   {
      long index = projectModel.locateEntry(key, forward);
      if (index != -1)
      {
         decorator.setKeyword(key, index);
         pageView.revalidate();
         pageView.repaint();
         pageView.ensureIndexIsVisible((int)index);
         pageView.setSelectedIndex((int)index);
      }
      return index;
   }

   public boolean nextPage() throws IOException
   {
      boolean result = projectModel.nextPage();
      return result;
   }

   public boolean prevPage() throws IOException
   {
      return projectModel.prevPage();
   }

   public void gotoLine(long ln) throws Exception
   {
      long index = projectModel.gotoLine(ln);
      logger.debug("going to line {} at {}", ln, index);
      pageView.setSelectedIndex((int)index);
      pageView.ensureIndexIsVisible((int)index);
   }

   public long getCurrentPageNumber()
   {
      return projectModel.getCurrentPageNumber();
   }

   public void updatePageInfo()
   {
      UserEvent event = new UserEvent(this, UserEvent.UPDATE_STATUS, projectModel.getPageInfo());
      instance.broadcast(event);
   }

   public void lostOwnership(Clipboard clipboard, Transferable contents)
   {
   }

   public LineLocation getBookmarkAtLine(long line) throws PageOutOfScopeException
   {
      return projectModel.getBookmarkAtLine(line);
   }

   public String[] getLogSummary()
   {
      this.projectModel.tryGetTotalLine(logSummary);
      return logSummary;
   }
}
