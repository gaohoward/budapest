package org.apache.activemq.artemis.budapest.ui;

import org.apache.activemq.artemis.budapest.BudapestApp;
import org.apache.activemq.artemis.budapest.ui.instance.MainInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.io.IOException;
import java.net.URL;

public class MainWindow extends WindowAdapter
{
   private static final Logger logger = LoggerFactory.getLogger(MainWindow.class);

   private BudapestApp owner;
   private MainInstance instance;
   private JFrame frame;
   private JMenuBar menuBar;
   private JToolBar toolBar;
   private JPanel mainUiRoot;

   private Action newBrokerProjectAction;
   private Action exitAppAction;
   private Action helpAction;

   public MainWindow(BudapestApp mainApp) throws Exception {
      owner = mainApp;
      frame = new JFrame("Artemis Budapest");
      this.instance = new MainInstance(mainApp.getConfigManager());
   }

   public void run() throws IOException {
      final String iconLoc = "/icons/home.png";
      URL url = MainWindow.class.getResource(iconLoc);
      ImageIcon icon = new ImageIcon(url);
      frame.setIconImage(icon.getImage());

      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.addWindowListener(this);

      createActions();
      createMenusAndTollbars();

      mainUiRoot = new JPanel();
      mainUiRoot.setLayout(new BorderLayout());

      this.instance.initUI(mainUiRoot);

      frame.getContentPane().add(mainUiRoot, BorderLayout.CENTER);

      frame.setSize(1024, 768);
      frame.setVisible(true);
   }

   private void createActions() {
      this.newBrokerProjectAction = new AbstractAction("New", null) {
         @Override
         public void actionPerformed(ActionEvent e) {
            instance.createNewBrokerProject();
         }
      };
      this.exitAppAction = new AbstractAction("Exit", null) {
         @Override
         public void actionPerformed(ActionEvent e) {
           frame.dispose();
         }
      };
      this.helpAction = new AbstractAction("Help", null) {
         @Override
         public void actionPerformed(ActionEvent e) {
            instance.showHelp();
         }
      };
   }

   private void createMenusAndTollbars()
   {
      menuBar = new JMenuBar();
      JMenu mainMenu = new JMenu("Main");
      mainMenu.add(newBrokerProjectAction);
      mainMenu.add(exitAppAction);
      mainMenu.add(helpAction);
      menuBar.add(mainMenu);

      toolBar = new JToolBar();
      toolBar.add(newBrokerProjectAction);
      toolBar.add(helpAction);

      frame.setJMenuBar(menuBar);
      frame.getContentPane().add(toolBar, BorderLayout.NORTH);
   }

   public JFrame getFrame()
   {
      return this.frame;
   }

}
