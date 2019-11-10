package org.apache.activemq.artemis.budapest.ui.instance;

import org.apache.activemq.artemis.budapest.ui.UIHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JPopupMenu;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * there are dup code copied from RichLogEntryRenederer,
 * refactor later.
 */
public class JDataDetailPane extends JTextPane
{
   private static final Logger logger = LoggerFactory.getLogger(JDataDetailPane.class);
   private static final long serialVersionUID = -1008681913527577703L;

   private DataExplorationView owner;
   private StyledDocument document;
   private RichTextRenderer renderer;
   private List<Highlight> localHighlights;

   private JPopupMenu actionMenu = new JPopupMenu();
   private CopyLogActionMenu copyLogItem = new CopyLogActionMenu("Copy All");
   private SearchLogActionMenu searchItem = new SearchLogActionMenu("Search...");
   private CopyLogActionMenu copyItem = new CopyLogActionMenu("Copy");

   public JDataDetailPane(DataExplorationView owner)
   {
      this.owner = owner;
      document = this.getStyledDocument();
      renderer = new RichTextRenderer(document, UIHelper.isHidpi() ? 28 : 14);
      localHighlights = new ArrayList<Highlight>();
      this.addMouseListener(new MouseAdapter() {
         public void mousePressed(MouseEvent e) {
            showPopup(e);
         }
         public void mouseReleased(MouseEvent e) {
            showPopup(e);
         }
      });
      copyLogItem.addActionListener(new LogActionListener(copyLogItem));
      searchItem.addActionListener(new LogActionListener(searchItem));
      copyItem.addActionListener(new LogActionListener(copyItem));
   }

   private void showPopup(MouseEvent e)
   {
      if (!e.isPopupTrigger())
      {
         return;
      }
      actionMenu.removeAll();

      boolean showMenu = false;
      try
      {
         String record = document.getText(0, document.getLength());
         if (record != null && !record.trim().isEmpty())
         {
            copyLogItem.setData(record.trim());
            actionMenu.add(copyLogItem);
            showMenu = true;
         }
      }
      catch (BadLocationException e1)
      {
      }

      String selected = this.getSelectedText();
      if (selected != null && !selected.isEmpty())
      {
         searchItem.setData(selected.trim());
         actionMenu.add(searchItem);
         copyItem.setData(selected.trim());
         actionMenu.add(copyItem);
         showMenu = true;
      }

      if (showMenu)
      {
         actionMenu.show(e.getComponent(), e.getX(), e.getY());
      }
   }

   public void updateContent(String newContent) throws BadLocationException
   {
      localHighlights.clear();
      document.remove(0, document.getLength());
      renderer.formatText(newContent, localHighlights);
   }

   public String toString()
   {
      return renderer.toString();
   }

   private class SearchLogActionMenu extends LogActionMenu<String>
   {
      private static final long serialVersionUID = 7747204628347439175L;

      public SearchLogActionMenu(String label)
      {
         super(label);
      }

      @Override
      public void execute()
      {
         String key = data.trim();
         String escaped = key.replaceAll("\\s", "\\\\ ")
                  .replaceAll("\\(", "\\\\\\(")
                  .replaceAll("\\)", "\\\\\\)")
                  .replaceAll("!", "\\\\!")
                  .replaceAll("&", "\\\\&")
                  .replaceAll("\\|", "\\\\|");
         owner.queryProject(escaped);
      }
   }
}
