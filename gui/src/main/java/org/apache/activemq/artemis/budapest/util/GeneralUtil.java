package org.apache.activemq.artemis.budapest.util;

//as long as the vm restarted within one milli second
//the id is unique across whole life time
//for one application.
public class GeneralUtil
{
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

}
