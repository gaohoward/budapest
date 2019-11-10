package org.apache.activemq.artemis.budapest.iterator;

public class ByteUtil {

   public static int toIntValue(byte value)
   {
      return 0x000000FF & value;
   }
}
