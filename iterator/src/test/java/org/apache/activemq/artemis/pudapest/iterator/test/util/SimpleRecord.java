package org.apache.activemq.artemis.pudapest.iterator.test.util;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public class SimpleRecord
{
   public static final byte TRACE = 1;
   public static final byte DEBUG = 2;
   public static final byte INFO = 3;
   public static final byte WARN = 4;
   public static final byte ERROR = 5;

   protected int type;
   protected String message;
   
   public SimpleRecord(int type)
   {
      if (!isValidType(type))
      {
         throw new IllegalArgumentException("Invalid type: " + type);
      }
      this.type = type;
   }

   public String getMessage()
   {
      return this.message;
   }

   //don't read type
   public void readRecord(DataInputStream stream) throws IOException
   {
      int length = stream.readInt();
      byte[] bytes = new byte[length];
      stream.read(bytes);
      message = new String(bytes, "UTF-8");
   }

   public String toString()
   {
      return message;
   }

   public String toDetails()
   {
      String recordType = "UNKNOWN";
      switch (type)
      {
      case ERROR:
         recordType = "ERROR";
         break;
      case WARN:
         recordType = "WARN";
         break;
      case INFO:
         recordType = "INFO";
         break;
      case DEBUG:
         recordType = "DEBUG";
         break;
      case TRACE:
         recordType = "TRACE";
         break;
      }
      return "[" + recordType + ":" + message + "]";
   }

   public static boolean isValidType(int recordType)
   {
      return (recordType >= TRACE) && (recordType <= ERROR);
   }
   
   public void setMessage(String message)
   {
      this.message = message;
   }

   public ByteBuffer getBuffer() throws UnsupportedEncodingException
   {
      byte[] body = message.getBytes("UTF-8");
      ByteBuffer buffer = ByteBuffer.allocate(body.length + 5);
      buffer.position(0);
      buffer.put((byte)type);
      buffer.putInt(body.length);
      buffer.put(body);
      buffer.position(0);
      return buffer;
   }
}
