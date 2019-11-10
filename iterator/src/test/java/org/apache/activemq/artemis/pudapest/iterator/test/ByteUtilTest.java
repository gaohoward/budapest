package org.apache.activemq.artemis.pudapest.iterator.test;

import org.apache.activemq.artemis.budapest.iterator.ByteUtil;
import org.junit.Assert;
import org.junit.Test;

public class ByteUtilTest extends Assert
{
   @Test
   public void testToIntValue() throws Exception
   {
      for (byte value = Byte.MIN_VALUE; value < Byte.MAX_VALUE; value++)
      {
         System.out.println("value of " + value + " is " + ByteUtil.toIntValue(value));
      }
   }

}
