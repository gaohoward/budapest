package org.apache.activemq.artemis.budapest.project.book;

public class PageInfo extends StatusInfo
{
   public PageInfo(long pg, long ln)
   {
      statusMap.put(PAGENO, pg);
      statusMap.put(LINENO, ln);
   }
}
