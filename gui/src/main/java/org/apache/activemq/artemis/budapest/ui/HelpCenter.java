package org.apache.activemq.artemis.budapest.ui;

import java.net.URL;

public class HelpCenter
{
   public HelpCenter()
   {
   }

   public URL getHelpUrl()
   {
      final String htmlLoc = "/help/help.html";
      URL url = this.getClass().getResource(htmlLoc);
      return url;
   }
}
