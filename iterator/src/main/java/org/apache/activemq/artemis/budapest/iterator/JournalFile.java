package org.apache.activemq.artemis.budapest.iterator;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Abstract notion for a Journal File
 * A Journal File is one of a set of files that holds records
 * The contents of such a file often includes in order:
 * 
 * Header -> a certain bytes of data usually contains identity information of the file
 *           like an ID, etc
 * Records -> a series of data records. padding bytes may exist between records.
 * 
 * Unused space -> spare space where more records can be saved. Unused spaces must be the
 * last consecutive space of the file. 
 * 
 * @author howard
 *
 */
public abstract class JournalFile implements Comparable<JournalFile>
{
   protected File file;
   
   protected RandomAccessFile handle = null;
   protected FileChannel channel = null;
   protected boolean isOpen;

   public JournalFile(File file) throws IOException
   {
      this.file = file;
   }
   
   public String toString()
   {
      return "[JournalFile] " + file.getName();
   }

   public File getFile()
   {
      return file;
   }

   public boolean isOpen()
   {
      return isOpen;
   }

   private void checkOpen() throws IOException
   {
      if (!isOpen)
      {
         throw new IllegalStateException("file not open " + file.getName());
      }
   }

   //set the file pointer to the absolute position
   public void position(long position) throws IOException
   {

      checkOpen();

      if (position == 0)
      {
         channel.position(getFileHeaderLength());
      }
      else if (position < getFileHeaderLength())
      {
         throw new IOException("Position should not be less then file header length: " + position);
      }
      else
      {
         channel.position(position);
      }
   }

   public void open(String mode) throws IOException
   {
      if (isOpen) return;
      handle = new RandomAccessFile(file, mode);
      channel = handle.getChannel();
      init();
      isOpen = true;
   }

   //first thing to do when a journal file is opened.
   //subclasses may use it to read headers
   protected abstract void init() throws IOException;

   protected abstract long getFileHeaderLength();

   public void close() throws IOException
   {
      if (!isOpen) return;
      channel.close();
      handle.close();
      channel = null;
      handle = null;
      isOpen = false;
   }

   public long position() throws IOException
   {
      if (isOpen)
      {
        return channel.position();
      }
      return -1L;
   }

   public int read(ByteBuffer buffer) throws IOException
   {
      checkOpen();
      return channel.read(buffer);
   }

   //is the last read byte a padding or unused, or invalid?
   //the caller makes sure that the byte is outside any record
   //so it cannot be a 'bad' byte inside a record. This makes
   //it a lot easier for the journal file to find out the nature
   //of this byte.
   //return false only if the byte is invalid.
   public abstract ByteType ignoreLastByte(int theByte);
}
