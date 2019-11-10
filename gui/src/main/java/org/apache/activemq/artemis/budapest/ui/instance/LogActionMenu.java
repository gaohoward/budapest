package org.apache.activemq.artemis.budapest.ui.instance;

import javax.swing.JMenuItem;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;

public class LogActionMenu<T> extends JMenuItem implements ClipboardOwner
{
   protected T data;

   public LogActionMenu(String label)
   {
      super(label);
   }

   public void setData(T data)
   {
      this.data = data;
   }

   public void execute()
   {
   }

   @Override
   public void lostOwnership(Clipboard clipboard, Transferable contents)
   {
   }
}
