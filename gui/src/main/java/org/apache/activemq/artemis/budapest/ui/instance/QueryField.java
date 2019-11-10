package org.apache.activemq.artemis.budapest.ui.instance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;

public class QueryField
{
   private static final Logger logger = LoggerFactory.getLogger(QueryField.class);
   JComboBox<String> queryField = new JComboBox<String>();
   DefaultComboBoxModel<String> model = new DefaultComboBoxModel<String>();
   Set<String> history = new HashSet<String>();
   JPopupMenu actionMenu = new JPopupMenu();
   PasteLogActionMenu pasteItem = new PasteLogActionMenu("Paste");

   public QueryField()
   {
      queryField.setModel(model);
      queryField.setEditable(true);

      queryField.getEditor().getEditorComponent().addMouseListener(new MouseAdapter() {
         public void mousePressed(MouseEvent e) {
            showPopup(e);
         }
         public void mouseReleased(MouseEvent e) {
            showPopup(e);
         }
      });

      pasteItem.addActionListener(new LogActionListener(pasteItem));
   }

   private void showPopup(MouseEvent e)
   {
      if (!e.isPopupTrigger())
      {
         return;
      }

      Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      Transferable transf = clipboard.getContents(this);
      logger.info("trasferrable: " + transf);
      String data = null;
      if (transf != null)
      {
         DataFlavor[] flavors = transf.getTransferDataFlavors();
         if (flavors != null)
         {
            for (DataFlavor df : flavors)
            {
               if (df.isFlavorTextType())
               {
                  try
                  {
                     data = (String) transf.getTransferData(df);
                     break;
                  }
                  catch (Exception e1)
                  {
                  }
               }
            }
         }
      }

      boolean showMenu = false;
      actionMenu.removeAll();
      if (data != null && !data.trim().isEmpty())
      {
         this.pasteItem.setData(data);
         actionMenu.add(pasteItem);
         showMenu = true;
      }

      if (showMenu)
      {
         actionMenu.show(e.getComponent(), e.getX(), e.getY());
      }
   }

   public void addActionListener(ActionListener actionListener)
   {
      queryField.addActionListener(actionListener);
   }

   public JComponent getControl()
   {
      return queryField;
   }

   public String getText()
   {
      return queryField.getSelectedItem().toString();
   }

   public void setText(String string)
   {
      queryField.setSelectedItem(string);
   }

   public void addHistory(String item)
   {
      if (history.add(item))
      {
         model.addElement(item);
      }
   }

   private class PasteLogActionMenu extends LogActionMenu<String>
   {
      public PasteLogActionMenu(String label)
      {
         super(label);
      }

      @Override
      public void execute()
      {
         queryField.setSelectedItem(data);
      }
   }

}
