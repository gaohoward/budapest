package org.apache.activemq.artemis.budapest.ui.instance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JScrollPane;

public class InfoConsole extends JScrollPane {

   private static final long serialVersionUID = 1603434469581498348L;
   private static final Logger logger = LoggerFactory.getLogger(InfoConsole.class);
   public void showInfo(String[] logSummary)
   {
      for (String s : logSummary)
      {
         //logger.info(s);
      }
   }

}
