package org.apache.activemq.artemis.budapest.config;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import org.apache.activemq.artemis.budapest.BudapestApp;
import org.apache.activemq.artemis.budapest.util.XmlConfigUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/*
Example:

<configuration>
  <ui>
    <default-font size="24"/>'
  </ui>
  <budapest-projects>
    <project base-dir="/home/howard/tests/apache-artemis-2.11.0-SNAPSHOT/bin/broker0" id="1573292314335" is-artemis-instance="true" name="broker0" parent-id="" type="1">
      <children>
        <child id="1573292314336"/>
        <child id="1573292314337"/>
        <child id="1573292314338"/>
        <child id="1573292314339"/>
      </children>
    </project>
    <project base-dir="/home/howard/tests/apache-artemis-2.11.0-SNAPSHOT/bin/broker0/data/journal" id="1573292314336" name="Journal" parent-id="1573292314335" type="2">
      <children>
        <child id="1573293272437"/>
      </children>
    </project>
    <project base-dir="/home/howard/tests/apache-artemis-2.11.0-SNAPSHOT/bin/broker0/data/bindings" id="1573292314337" name="Bindings" parent-id="1573292314335" type="3">
      <children/>
    </project>
    <project base-dir="/home/howard/tests/apache-artemis-2.11.0-SNAPSHOT/bin/broker0/data/paging" id="1573292314338" name="Paging" parent-id="1573292314335" type="4">
      <children/>
    </project>
    <project base-dir="/home/howard/tests/apache-artemis-2.11.0-SNAPSHOT/bin/broker0/data/large-messages" id="1573292314339" name="Large Messages" parent-id="1573292314335" type="5">
      <children/>
    </project>
  <project filter-type="0" filter-value="390\ bytes" id="1573293272437" name="390\ bytes" parent-id="1573292314336" type="6">
      <children/>
    </project>

  </budapest-projects>
</configuration>


*/
public class ConfigManager
{
   private static final Logger logger = LoggerFactory.getLogger(ConfigManager.class);
   private BudapestApp mainApp;
   private File configFile;
   private File configDir;
   private MainConfig mainConfig;
   private Document document;
   private UIConfig uiConfig;

   public ConfigManager(BudapestApp mainApp)
   {
      this.mainApp = mainApp;
   }

   private void createEmptyConfig() throws Exception
   {
      configFile.createNewFile();
      PrintWriter out = new PrintWriter(new FileWriter(configFile));
      out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
      out.println("<configuration>");
      out.println("  <ui>");
      out.println("  </ui>");
      out.println("  <budapest-projects>");
      out.println("  </budapest-projects>");
      out.println("</configuration>");
      out.flush();
      out.close();
   }

   public File getConfigDir()
   {
      return configDir;
   }

   public void start() throws Exception
   {
      logger.info("Starting config manager....");
      configDir = mainApp.getConfigDir();

      System.out.println("configir: " + configDir.getAbsolutePath());
      configFile = new File(configDir, "configuration.xml");
      if (!configFile.exists())
      {
         createEmptyConfig();
      }

      logger.info("Parsing config: " + configFile.getAbsolutePath());
      try (InputStream configInput = new FileInputStream(configFile))
      {
         DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
         DocumentBuilder domBuilder = domFactory.newDocumentBuilder();
         document = domBuilder.parse(configInput);

         Element uiElem = (Element) document.getElementsByTagName("ui").item(0);
         uiConfig = new UIConfig(uiElem);

         Element mainElem = (Element) document.getElementsByTagName("budapest-projects").item(0);
         mainConfig = new MainConfig(this, mainElem);
      }
      catch (ParserConfigurationException | IOException | SAXException e)
      {
         logger.error("Error loading configuration.", e);
      }

      logger.info("config manager started");
   }

   public MainConfig getMainConfig()
   {
      return this.mainConfig;
   }

   public UIConfig getUIConfig()
   {
      return this.uiConfig;
   }

   public void writeConfig() throws Exception {
      XmlConfigUtil.writeConfig(document, configFile);
   }

   public File getInstanceDataDir(String name) {
      return mainApp.getInstanceDir(name);
   }
}
