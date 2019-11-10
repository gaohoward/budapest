package org.apache.activemq.artemis.pudapest.iterator.test.util;

import org.apache.activemq.artemis.budapest.iterator.ByteType;
import org.apache.activemq.artemis.budapest.iterator.JournalFile;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/*
 * A Simple Journal File
 * Header: 16 bytes, where the first 4 bytes are file id, used for sorting
 * second 4 bytes used for file size. The rest are not used for the moment
 * Record: 1 byte : record type (non zero); 4 bytes: body length; body: contents
 * Padding: 80 ('P')
 * Unused space: 0 (if zero record type is met, then you can be sure that all the rest bytes
 * are not used).
 */
public class SimpleJournalFile extends JournalFile {

   static final int HEADER = 16;

   int fileId;
   int size;
   
   boolean writeMode;
   
   public SimpleJournalFile(File file) throws IOException
   {
      super(file);
   }

   @Override
   protected void init() throws IOException
   {
      ByteBuffer buffer = ByteBuffer.allocate(HEADER);
      channel.position(0);
      int n = channel.read(buffer);
      if (n != 16)
      {
         throw new IllegalStateException("Not enough bytes for header: " + n);
      }
      buffer.position(0);
      fileId = buffer.getInt();
      size = buffer.getInt();
   }

   @Override
   public int compareTo(JournalFile arg0)
   {
      SimpleJournalFile simple = (SimpleJournalFile)arg0;
      return fileId - simple.fileId;
   }

   @Override
   protected long getFileHeaderLength()
   {
      return HEADER;
   }

   @Override
   public ByteType ignoreLastByte(int theByte)
   {
      if (theByte == 'P')
      {
         return ByteType.PADDING;
      }
      else if (theByte == 0)
      {
         return ByteType.UNUSED;
      }
      return ByteType.INVALID;
   }

   
   public static SimpleJournalFile format(File journal, int fileId, int size) throws IOException
   {
      if (!journal.exists())
      {
         journal.createNewFile();
      }

      RandomAccessFile handle = new RandomAccessFile(journal, "rw");
      FileChannel channel = handle.getChannel();

      final int buffSize = 10240;//the larger the size the faster the format.
      ByteBuffer buffer = ByteBuffer.allocateDirect(buffSize);
      for (int i = 0; i < buffSize; i++)
      {
         buffer.put((byte)0);
      }
      buffer.position(0);

      long wrtSz = 0;
      while (wrtSz < size)
      {
         long remain = size - wrtSz;
         if (remain < buffSize)
         {
            ByteBuffer newBuffer = ByteBuffer.allocateDirect((int)remain);
            for (int i = 0; i < remain; i++)
            {
               newBuffer.put((byte)0);
            }
            newBuffer.position(0);
            channel.write(newBuffer);
            break;
         }
         int n = channel.write(buffer);
         buffer.position(0);
         wrtSz += n;
      }
      
      //now write fileId
      buffer = ByteBuffer.allocateDirect(8);
      buffer.putInt(fileId);
      buffer.putInt(size);
      buffer.position(0);
      
      channel.position(0);
      channel.write(buffer);
      
      channel.close();
      handle.close();
      
      return new SimpleJournalFile(journal);
   }

   //return false if it can't hold this record.
   public boolean writeRecord(SimpleRecord r) throws IOException
   {
      checkWrite();
      
      ByteBuffer buffer = r.getBuffer();
      
      if (channel.position() + buffer.limit() > size)
      {
         return false;
      }

      channel.write(buffer);
      return true;
   }

   private void checkWrite()
   {
      if (!writeMode)
      {
         throw new IllegalStateException("Journal not in write mode!");
      }
   }
   
   public void beginWrite() throws IOException
   {
      //seek to the last record
      channel.position(HEADER);
      SimpleJournalRecordDecoder.seekEnd(channel);
      writeMode = true;
   }
   
   public String toString()
   {
      return super.toString() + " id: " + fileId;
   }

   public void padding() throws IOException
   {
      int numPaddingBytes = (int) (size - channel.position());
      ByteBuffer buffer = ByteBuffer.allocateDirect(1);
      buffer.put((byte)'P');

      for (int i = 0; i < numPaddingBytes; i++)
      {
         buffer.position(0);
         channel.write(buffer);
      }
   }
}
