package org.apache.activemq.artemis.budapest;

import org.apache.activemq.artemis.budapest.config.ConfigManager;
import org.apache.activemq.artemis.budapest.config.UIConfig;
import org.apache.activemq.artemis.budapest.ui.MainWindow;
import org.apache.activemq.artemis.budapest.ui.UIHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.io.File;

public class BudapestApp {

   private static final Logger logger = LoggerFactory.getLogger(BudapestApp.class);

   private MainWindow mainWindow;
   private ConfigManager configManager;
   private String appHome;
   private CountDownLatch uiLatch = new CountDownLatch(1);

   public static void main(String[] args) throws Exception {
      new BudapestApp(args).launch();
   }

   public BudapestApp(String[] args) throws Exception {
      if (args.length > 0)
      {
         appHome = args[0];
      }
      else
      {
         appHome = System.getProperty("user.dir");
      }
      configManager = new ConfigManager(this);
      configManager.start();
   }

   public void launch() throws Exception
   {
      UIConfig uiConfig = configManager.getUIConfig();
      FontUIResource resource = uiConfig.getDefaultFont();
      if (resource != null)
      {
         logger.info("Using custom font: " + resource);
         UIManager.put(UIHelper.MENU_FONT_KEY, resource);
         UIManager.put(UIHelper.MENU_ITEM_FONT_KEY, resource);
         UIManager.put(UIHelper.LIST_FONT_KEY, resource);
         UIManager.put(UIHelper.VIEW_PORT_FONT_KEY, resource);
         UIManager.put(UIHelper.TAB_PANE_FONT_KEY, resource);
         UIManager.put(UIHelper.SCROLL_PANE_FONT_KEY, resource);
         UIManager.put(UIHelper.CHECKBOX_MENU_ITEM_FONT_KEY, resource);
         UIManager.put(UIHelper.FORMATTED_TEXT_FIELD_FONT_KEY, resource);
         UIManager.put(UIHelper.TEXT_FIELD_FONT_KEY, resource);
         UIManager.put(UIHelper.TITLED_BORDER_FONT_KEY, resource);
         UIManager.put(UIHelper.EDITOR_PANE_FONT_KEY, resource);
         UIManager.put(UIHelper.TREE_TREE_FONT_KEY, resource);
         UIManager.put(UIHelper.COMBOBOX_FONT_KEY, resource);
         UIManager.put(UIHelper.TEXTAREA_FONT_KEY, resource);
         UIManager.put(UIHelper.LABEL_FONT_KEY, resource);
         UIManager.put(UIHelper.BUTTON_FONT_KEY, resource);
         UIManager.put(UIHelper.POPUPMENU_FONT_KEY, resource);
         UIManager.put(UIHelper.TOOLTIP_FONT_KEY, resource);
         UIManager.put(UIHelper.TABLE_HEADER_FONT_KEY, resource);
         UIManager.put(UIHelper.TEXT_PANE_FONT_KEY, resource);
         UIManager.put(UIHelper.MENUBAR_FONT_KEY, resource);
         UIManager.put(UIHelper.INTERNAL_FRAME_FONT_KEY, resource);
         UIManager.put(UIHelper.TOOLBAR_FONT_KEY, resource);
         UIManager.put(UIHelper.OPTION_PANE_FONT_KEY, resource);
         UIManager.put(UIHelper.TABLE_FONT_KEY, resource);
         UIManager.put(UIHelper.PANEL_FONT_KEY, resource);
      }
      logger.info("Launching application with home: " + appHome + " ... ");
      SwingUtilities.invokeLater(new Runnable() {
         @Override
         public void run() {
            try {
               setupUi();
            } catch (ClassNotFoundException e) {
               e.printStackTrace();
            } catch (UnsupportedLookAndFeelException e) {
               e.printStackTrace();
            } catch (InstantiationException e) {
               e.printStackTrace();
            } catch (IllegalAccessException e) {
               e.printStackTrace();
            } catch (IOException e) {
               e.printStackTrace();
            } catch (Exception e) {
               e.printStackTrace();
            } finally
            {
               uiLatch.countDown();
            }
         }
      });

      uiLatch.await();
      logger.info("Artemis Budapest is ready");
   }

   public void setupUi() throws Exception {
      //UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      logger.info("Updating ui.....");
      mainWindow = new MainWindow(this);
      mainWindow.run();
      SwingUtilities.updateComponentTreeUI(mainWindow.getFrame());
      logger.info("starting main window...");
   }

   public File getConfigDir()
   {
      File cfgDir = new File(appHome, "cfg");
      if (!cfgDir.exists())
      {
         cfgDir.mkdirs();
      }
      return cfgDir;
   }

   public File getInstanceDir(String instanceID)
   {
      File currentDir = new File(appHome);
      File workspaceBase = new File(currentDir, "workspace");
      File targetDir = new File(workspaceBase, instanceID);
      if (!targetDir.exists())
      {
         targetDir.mkdirs();
      }
      return targetDir;
   }

   public ConfigManager getConfigManager() {
      return this.configManager;
   }
}
