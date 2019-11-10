package org.apache.activemq.artemis.budapest.project.book;

import java.util.HashMap;

public class StatusInfo
{
   public static final String GENERAL = "general";
   public static final String PAGENO = "Pg";
   public static final String LINENO = "Ln";

   protected HashMap<String, Object> statusMap = new HashMap<String, Object>();

   public StatusInfo()
   {
   }

   public StatusInfo(String text)
   {
      statusMap.put(GENERAL, text);
   }

   public void addStatus(String key, Object val)
   {
      statusMap.put(key, val);
   }

   public HashMap<String, Object> getStatusMap()
   {
      return statusMap;
   }
}
