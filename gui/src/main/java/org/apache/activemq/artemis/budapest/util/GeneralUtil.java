package org.apache.activemq.artemis.budapest.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

//as long as the vm restarted within one milli second
//the id is unique across whole life time
//for one application.
public class GeneralUtil
{
   private static final Logger logger = LoggerFactory.getLogger(GeneralUtil.class);

   private static long lastTime = -1;

   public static synchronized String getTimeID()
   {
      if (lastTime == -1)
      {
         lastTime = System.currentTimeMillis();
      }
      else
      {
         lastTime++;
      }
      return String.valueOf(lastTime);
   }

   public static String bytesToHexString(byte[] bytes, int len)
   {
      StringBuilder builder = new StringBuilder();
      if (len > 0) {
         for (int i = 0; i < len; i++) {
            byte b = bytes[i];
            builder.append(String.format("%02X ", b));
            if (i > 0 && i%8 == 0)
            {
               builder.append(" ");
            }
         }
      }
      return builder.toString();
   }

   public static String parseAddress(File pageDir)
   {
      String theAddress = null;
      File addrFile = new File(pageDir, "address.txt");
      try (BufferedReader reader = new BufferedReader(new FileReader(addrFile)))
      {
         String ln = reader.readLine();
         while (ln != null)
         {
            if (ln.trim().length() > 0)
            {
               theAddress = ln.trim();
               break;
            }
            ln = reader.readLine();
         }
      }
      catch (IOException e1)
      {
         logger.warn("IOException when accessing file {}", addrFile, e1);
         theAddress = "UNKNOWN";
      }
      return theAddress;
   }

}
