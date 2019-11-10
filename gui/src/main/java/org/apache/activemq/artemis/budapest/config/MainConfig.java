package org.apache.activemq.artemis.budapest.config;

import org.apache.activemq.artemis.budapest.util.XmlConfigUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;

public class MainConfig {

   private ConfigManager manager;
   private Element rootElement;

   public MainConfig(ConfigManager configManager, Element mainElem) {
      this.manager = configManager;
      this.rootElement = mainElem;
   }

   public Element getRoot() {
      return this.rootElement;
   }

   public void writeConfig() throws Exception
   {
      manager.writeConfig();
   }

   public File getInstanceDir(String name) {
      return manager.getInstanceDataDir(name);
   }
}
