package org.apache.activemq.artemis.budapest.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.util.Enumeration;

public final class UIHelper
{
   private static final Logger logger = LoggerFactory.getLogger(UIHelper.class);

   public static final String MENU_FONT_KEY = "Menu.font";
   public static final String MENU_ITEM_FONT_KEY = "MenuItem.font";
   public static final String LIST_FONT_KEY = "List.font";
   public static final String VIEW_PORT_FONT_KEY = "Viewport.font";
   public static final String TAB_PANE_FONT_KEY = "TabbedPane.font";
   public static final String SCROLL_PANE_FONT_KEY = "ScrollPane.font";
   public static final String CHECKBOX_MENU_ITEM_FONT_KEY = "CheckBoxMenuItem.font";

   public static final String FORMATTED_TEXT_FIELD_FONT_KEY = "FormattedTextField.font";
   public static final String TEXT_FIELD_FONT_KEY = "TextField.font";
   public static final String TITLED_BORDER_FONT_KEY = "TitledBorder.font";
   public static final String EDITOR_PANE_FONT_KEY = "EditorPane.font";
   public static final String TREE_TREE_FONT_KEY = "Tree.font";
   public static final String COMBOBOX_FONT_KEY = "ComboBox.font";
   public static final String TEXTAREA_FONT_KEY = "TextArea.font";
   public static final String LABEL_FONT_KEY = "Label.font";
   public static final String BUTTON_FONT_KEY = "Button.font";
   public static final String POPUPMENU_FONT_KEY = "PopupMenu.font";
   public static final String TOOLTIP_FONT_KEY = "ToolTip.font";
   public static final String TABLE_HEADER_FONT_KEY = "TableHeader.font";
   public static final String TEXT_PANE_FONT_KEY = "TextPane.font";
   public static final String MENUBAR_FONT_KEY = "MenuBar.font";
   public static final String INTERNAL_FRAME_FONT_KEY = "InternalFrame.titleFont";
   public static final String TOOLBAR_FONT_KEY = "ToolBar.font";
   public static final String OPTION_PANE_FONT_KEY = "OptionPane.font";
   public static final String TABLE_FONT_KEY = "Table.font";
   public static final String PANEL_FONT_KEY = "Panel.font";


   public static final void listUIDefaults()
   {
      Enumeration keys = UIManager.getDefaults().keys();
      while (keys.hasMoreElements())
      {
         Object key = keys.nextElement();
         Object value = UIManager.get (key);
         logger.info("key: " + key + " value: " + value);
      }
   }

   public static boolean isHidpi()
   {
      int resolution = Toolkit.getDefaultToolkit().getScreenResolution();
      Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

      if (dim.width > 1920 && dim.height > 1080)
      {
         return true;
      }
      return false;
   }

   public static String getIconSize()
   {
      if (UIHelper.isHidpi())
      {
         return "32";
      }
      return "";
   }

   public static void listSystemFontFamilies()
   {
      String fonts[] = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();

      for ( int i = 0; i < fonts.length; i++ )
      {
         logger.info("[" + i + "] " + fonts[i]);
      }
   }

   public static void listFontStyles()
   {
      Enumeration fontStyles = StyleContext.getDefaultStyleContext().getStyleNames();
      while (fontStyles.hasMoreElements())
      {
         logger.info("style: " + fontStyles.nextElement());
      }
   }

   public static void main(String[] args)
   {
      listSystemFontFamilies();
   }
}
