package org.apache.activemq.artemis.budapest.ui.instance;

import org.apache.activemq.artemis.budapest.config.ConfigHelper;
import org.apache.activemq.artemis.budapest.config.ConfigManager;
import org.apache.activemq.artemis.budapest.project.AbstractProject;
import org.apache.activemq.artemis.budapest.project.book.StatusInfo;
import org.apache.activemq.artemis.budapest.project.internal.ArtemisDataProject;
import org.apache.activemq.artemis.budapest.ui.CommonUI;
import org.apache.activemq.artemis.budapest.ui.HelpCenter;
import org.apache.activemq.artemis.budapest.ui.UIHelper;
import org.apache.activemq.artemis.budapest.ui.workspace.DataWorkSpace;
import org.apache.activemq.artemis.budapest.ui.workspace.EventMember;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainInstance implements EventMember {

   private static final Logger logger = LoggerFactory.getLogger(MainInstance.class);

   private final String name = "Budapest";
   private HelpCenter helpCenter;
   private DataWorkSpace workspace;

   private DataProjectManagementView workspaceView;
   private DataExplorationView logExplorationView;

   private Set<EventMember> members = new HashSet<EventMember>();

   private ConfigHelper config;

   private LogStatusBar statusBar;

   private InfoConsole infoConsole;

   private JComponent container;

   //pools and scheduler should get from framework.
   private ExecutorService workPool = Executors.newCachedThreadPool();

   public MainInstance(ConfigManager configManager) throws Exception {
      this.config = new ConfigHelper(configManager.getMainConfig());
   }

   public void initUI(JPanel mainUiRoot) throws IOException {
      this.container = mainUiRoot;

      helpCenter = new HelpCenter();

      //todo: read config and load workspace
      File workDir = config.getInstanceDataDir(name);
      workspace = new DataWorkSpace(this, workDir);

      workspace.registerListener(this.config);

      workspaceView = new DataProjectManagementView(this);

      logExplorationView = new DataExplorationView(this);

      JSplitPane upperLeftSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
               workspaceView.getProjectPane(), workspaceView.getLogListPane());
      upperLeftSplitPane.setOneTouchExpandable(true);
      upperLeftSplitPane.setResizeWeight(0.5);

      JSplitPane upperSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
               upperLeftSplitPane, logExplorationView.getLogPane());

      upperSplitPane.setOneTouchExpandable(true);
      upperSplitPane.setResizeWeight(0.2);

      JDataDetailPane logDetailPane = logExplorationView.getLogDetailPane();
      JScrollPane logContainer = new JScrollPane(logDetailPane);

      JTabbedPane tabbedPane = new JTabbedPane();
      tabbedPane.setTabPlacement(JTabbedPane.BOTTOM);

      tabbedPane.add("Detail", logContainer);

      infoConsole = new InfoConsole();
      tabbedPane.add("Info", infoConsole);

      JSplitPane rootSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, upperSplitPane, tabbedPane);
      rootSplitPane.setResizeWeight(0.9);
      container.add(rootSplitPane);

      statusBar = new LogStatusBar(this, container);

      addEventMember(this);
      logger.info("instance initiated. " + logger.getClass().getClassLoader());
   }

   public void createNewBrokerProject() {
      File dataDir = CommonUI.inputDirDialog(getContainer(), "Choose a data dir");
      if (dataDir != null)
      {
         getWorkspace().createArtemisDataProject(dataDir.getName(), dataDir, true);
      }
   }

   public Executor getThreadPool()
   {
      return workPool;
   }

   public JComponent getContainer()
   {
      return container;
   }

   public ConfigHelper getConfig()
   {
      return config;
   }

   public void sendStatus(String status)
   {
      SwingUtilities.invokeLater(new Runnable()
      {
         @Override
         public void run()
         {
            statusBar.showStatus(status);
         }
      });
   }

   public int getIntConfig(String key, int def)
   {
      if (key.equals("log-page-font-size"))
      {
         if (UIHelper.isHidpi())
         {
            return 28;
         }
      }
      return def;
   }

   //time to clean resources
   public void beforeDestroy()
   {
      logger.info("shutdown plugin {} ...", this);
      workPool.shutdownNow();
      workspace.shutdown();
   }

   public void addEventMember(EventMember member)
   {
      members.add(member);
   }

   public void broadcast(UserEvent event)
   {
      Iterator<EventMember> iter = members.iterator();
      while (iter.hasNext())
      {
         EventMember m = iter.next();
         if (m != event.getEventSource())
         {
            m.receive(event);
         }
      }
   }

   public DataWorkSpace getWorkspace()
   {
      return workspace;
   }

   public void receive(UserEvent event)
   {
      if (event.getEventType() == UserEvent.UPDATE_STATUS)
      {
         statusBar.showStatus((StatusInfo)event.getEventData());
      }
   }

   public void openProject(AbstractProject proj) throws Exception
   {
      if (proj.supportIteration())
      {
         logExplorationView.openProject(proj);
      }
      else
      {
         proj.open();
      }
   }

   public void closeProject(ArtemisDataProject proj) throws Exception
   {
      logExplorationView.closeProject(proj);
   }

   public void showHelp()
   {
      logExplorationView.showHelpPanel();
   }

   public URL getHelpUrl()
   {
      return helpCenter.getHelpUrl();
   }

   public InfoConsole getInfoConsole()
   {
      return this.infoConsole;
   }
}
