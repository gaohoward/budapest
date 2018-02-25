package org.jboss.budapest.data.repository;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public abstract class DataFile<T extends DataFile> implements Comparable<T>, AutoCloseable {
   protected File file;

   protected RandomAccessFile handle = null;
   protected FileChannel channel = null;
   protected boolean isOpen;

   public DataFile(File file) throws IOException {
      this.file = file;
   }

   public String toString() {
      return "[JournalFile] " + file.getName();
   }

   public File getFile() {
      return file;
   }

   public boolean isOpen() {
      return isOpen;
   }

   private void checkOpen() throws IOException {
      if (!isOpen) {
         throw new IllegalStateException("file not open " + file.getName());
      }
   }

   //set the file pointer to the absolute position
   public void position(long position) throws IOException {

      checkOpen();

      if (position == 0) {
         channel.position(getFileHeaderLength());
      }
      else if (position < getFileHeaderLength()) {
         throw new IOException("Position should not be less then file header length: " + position);
      }
      else {
         channel.position(position);
      }
   }

   public void open(String mode) throws IOException {
      if (isOpen) return;
      handle = new RandomAccessFile(file, mode);
      channel = handle.getChannel();
      init();
      isOpen = true;
   }

   //first thing to do when a data file is opened.
   //subclasses may use it to read headers
   protected abstract void init() throws IOException;

   protected abstract long getFileHeaderLength();

   public void close() throws IOException {
      if (!isOpen) return;
      channel.close();
      handle.close();
      channel = null;
      handle = null;
      isOpen = false;
   }

   public long position() throws IOException {
      if (isOpen) {
         return channel.position();
      }
      return -1L;
   }

   public int read(ByteBuffer buffer) throws IOException {
      checkOpen();
      return channel.read(buffer);
   }

   private ByteBuffer intBuffer = ByteBuffer.allocate(4);

   public int readInt() throws IOException {
      intBuffer.clear();
      int num = channel.read(intBuffer);
      if (num < 4) {
         throw new IOException("not enough bytes " + num);
      }
      return intBuffer.getInt();
   }

}
