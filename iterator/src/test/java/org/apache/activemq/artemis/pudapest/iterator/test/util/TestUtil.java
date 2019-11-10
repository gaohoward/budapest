package org.apache.activemq.artemis.pudapest.iterator.test.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public final class TestUtil
{
   private static SimpleDateFormat format = new SimpleDateFormat("MM:dd-HH:mm:ss:SSS");

   public static String getCurrentTime()
   {
      Date d = new Date();
      return format.format(d);
   }

   public void sampleInfo() throws Exception
   {
      System.out.println("Index at 5 position 555555");
      System.out.println("---------------------->----------------------");
      System.out.println("| 99999999 : 99999995 - 99999996 + 99999999 |");
      System.out.println("------------==========-==========^-----------");
      System.out.println("^  current pointer");
      System.out.println("=  the holder");
      System.out.println(">  forward iteration direction");
      System.out.println("<  backward iteration direction");
      System.out.println(":  a bookmark");
      System.out.println("-  journal file boundary");
      System.out.println("+  both a bookmark and a file boundary");
   }

}
