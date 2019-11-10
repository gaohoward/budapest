package org.apache.activemq.artemis.pudapest.iterator.test.util;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Random;

import org.apache.activemq.artemis.budapest.iterator.ByteType;
import org.apache.activemq.artemis.budapest.iterator.DecodeError;
import org.apache.activemq.artemis.budapest.iterator.JournalInputStream;
import org.apache.activemq.artemis.budapest.iterator.JournalRecordDecoder;

public class SimpleJournalRecordDecoder implements JournalRecordDecoder<SimpleRecord>
{
   private Random r = new Random(10);
   /**
    * Record format: 1 byte : record type (non zero); 4 bytes: body length; body: contents
    * types: see SimpleRecord
    */
   @Override
   public SimpleRecord decode(JournalInputStream stream) throws DecodeError
   {
      SimpleRecord record = null;
      int recordType = 0;
      try
      {
         recordType = stream.read();
         while (!SimpleRecord.isValidType(recordType))
         {
            if (stream.verifyUnknownByte(recordType) == ByteType.PADDING)
            {
               recordType = stream.read();
            }
            else if (stream.verifyUnknownByte(recordType) == ByteType.UNUSED)
            {
               return null;
            }
            else
            {
               throw new DecodeError("Unrecognized record type: " + recordType);
            }
         }
      }
      catch (IOException e)
      {
         throw new DecodeError("Exception in decoding type", e);
      }

      record = new SimpleRecord(recordType);
      try
      {
         record.readRecord(stream);
      }
      catch (IOException e)
      {
         throw new DecodeError("Error decoding record body.", e);
      }
      
      return record;
   }

   public SimpleRecord createRandomRecord(int id)
   {
      int type = r.nextInt(5) + 1;
      SimpleRecord record = new SimpleRecord(type);
//      record.setMessage(TestUtil.getCurrentTime()  + "i" + id + "i");
      record.setMessage("i" + id + "i");
      return record;
   }

   public static void seekEnd(FileChannel channel) throws IOException
   {
      ChannelStream stream = new ChannelStream(channel);
      
      int recordType = stream.read();
      SimpleRecord record = null;
      
      while (SimpleRecord.isValidType(recordType))
      {
         record = new SimpleRecord(recordType);
         record.readRecord(new DataInputStream(stream));

         recordType = stream.read();
      }

      long cur = channel.position();
      channel.position(cur - 1);
   }
   
   private static class ChannelStream extends InputStream
   {
      private FileChannel channel;
      private ByteBuffer buffer = ByteBuffer.allocateDirect(1);
      
      public ChannelStream(FileChannel channel)
      {
         this.channel = channel;
      }

      @Override
      public int read() throws IOException
      {
         buffer.position(0);
         channel.read(buffer);
         return buffer.get(0);
      }
      
   }

}
