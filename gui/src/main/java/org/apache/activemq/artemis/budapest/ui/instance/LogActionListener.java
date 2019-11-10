package org.apache.activemq.artemis.budapest.ui.instance;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LogActionListener implements ActionListener
{
   private LogActionMenu source;

   public LogActionListener(LogActionMenu source)
   {
      this.source = source;
   }

   @Override
   public void actionPerformed(ActionEvent e)
   {
      this.source.execute();
   }
}
