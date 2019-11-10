package org.apache.activemq.artemis.budapest.config;

import org.apache.activemq.artemis.budapest.ui.UIHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;

public class UIConfig
{
   private static final Logger logger = LoggerFactory.getLogger(UIConfig.class);

   private FontUIResource defaultFont;

   public UIConfig(Element uiElem)
   {
      if (uiElem != null)
      {
         NodeList listNode = uiElem.getElementsByTagName("default-font");
         if (listNode.getLength() > 0)
         {
            Element fontElem = (Element) listNode.item(0);
            String value = fontElem.getAttribute("size");
            int fsize = Integer.valueOf(value);

            Object fontObj = UIManager.get(UIHelper.MENU_FONT_KEY);
            if (fontObj instanceof FontUIResource)
            {
               FontUIResource res = (FontUIResource) fontObj;

               if (fsize > 0 && fsize != res.getSize())
               {
                  defaultFont = new FontUIResource(res.getFontName(), res.getStyle(), fsize);
               }
               else
               {
                  defaultFont = res;
               }
            }
         }
      }
   }

   public FontUIResource getDefaultFont()
   {
      return this.defaultFont;
   }

}
