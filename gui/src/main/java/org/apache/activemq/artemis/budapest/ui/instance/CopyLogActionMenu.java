package org.apache.activemq.artemis.budapest.ui.instance;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

public class CopyLogActionMenu extends LogActionMenu<String>
{
   public CopyLogActionMenu(String label)
   {
      super(label);
   }

   @Override
   public void execute()
   {
      StringSelection stringSelection = new StringSelection(data);
      Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      clipboard.setContents(stringSelection, this);
   }
}
