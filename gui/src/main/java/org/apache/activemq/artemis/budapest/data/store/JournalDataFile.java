package org.apache.activemq.artemis.budapest.data.store;

import org.apache.activemq.artemis.budapest.iterator.ByteType;
import org.apache.activemq.artemis.budapest.iterator.JournalFile;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public class JournalDataFile extends JournalFile {

   public static final int SIZE_HEADER = 16;

   //this is a nominal file for a journal.
   //usually this is the same as the real journal file
   private String filePath;

   private int journalVersion;
   private int userVersion;
   private long fileID;

   public JournalDataFile(File file) throws IOException {
      super(file);
      this.filePath = file.getAbsolutePath();
   }

   @Override
   //JournalFileImpl.readFileHeader
   protected void init() throws IOException {

      ByteBuffer bb = ByteBuffer.allocate(SIZE_HEADER);
      int n = channel.read(bb);

      bb.rewind();

      this.journalVersion = bb.getInt();
      this.userVersion = bb.getInt();
      this.fileID = bb.getLong();
   }

   @Override
   protected long getFileHeaderLength() {
      return SIZE_HEADER;
   }

   @Override
   //org.apache.activemq.artemis.core.journal.impl.JournalImpl.JournalFileComparator
   public int compareTo(JournalFile other) {
      JournalDataFile o = (JournalDataFile)other;
      int result = this.fileID < o.fileID ? -1 : this.fileID == o.fileID ? 0 : 1;
      return result;
   }

   @Override
   public int hashCode() {
      return this.filePath.hashCode();
   }

   @Override
   public boolean equals(Object obj) {
      return this.filePath.equals(obj);
   }

   public String getName() {
      return this.filePath;
   }

   public void setName(File file) {
      this.filePath = file.getAbsolutePath();
   }

   @Override
   public ByteType ignoreLastByte(int arg0)
   {
      throw new IllegalStateException("No byte should be ignored!");
   }
}
