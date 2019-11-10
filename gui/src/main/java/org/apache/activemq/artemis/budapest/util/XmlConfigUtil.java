package org.apache.activemq.artemis.budapest.util;

import java.io.File;
import java.io.FileOutputStream;

import org.w3c.dom.Document;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class XmlConfigUtil
{
   public static void writeConfig(Document doc, File target) throws Exception
   {
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = transformerFactory.newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
      DOMSource source = new DOMSource(doc);
      try (FileOutputStream outStream = new FileOutputStream(target))
      {
         StreamResult result =  new StreamResult(outStream);
         transformer.transform(source, result);
      }
   }

}
