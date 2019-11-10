package org.apache.activemq.artemis.budapest.iterator;

import java.io.IOException;

public class DecodeError extends Exception {

   private static final long serialVersionUID = -1082847510596028241L;

   public DecodeError(String msg, IOException e)
   {
      super(msg, e);
   }

   public DecodeError(String msg)
   {
      super(msg);
   }

}
