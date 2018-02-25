package org.jboss.budapest.data.journal;

import org.apache.activemq.artemis.core.journal.impl.JournalImpl;
import org.apache.activemq.artemis.journal.ActiveMQJournalBundle;
import org.jboss.budapest.data.repository.DataFile;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public class JournalDataFile extends DataFile<JournalDataFile> {

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

      ByteBuffer bb = ByteBuffer.allocate(JournalImpl.SIZE_HEADER);
      int n = channel.read(bb);
      System.out.println("bytes read: " + n);

      bb.rewind();

      this.journalVersion = bb.getInt();
      System.out.println("journal version: " + this.journalVersion);
      this.userVersion = bb.getInt();
      System.out.println("user version: " + this.userVersion);
      this.fileID = bb.getLong();
      System.out.println("file id: " + this.fileID);
   }

   @Override
   protected long getFileHeaderLength() {
      return JournalImpl.SIZE_HEADER;
   }

   @Override
   //org.apache.activemq.artemis.core.journal.impl.JournalImpl.JournalFileComparator
   public int compareTo(JournalDataFile o) {
      int result = this.fileID < o.fileID ? -1 : this.fileID == o.fileID ? 0 : 1;
      System.out.println("---comparing " + this.fileID + " to " + o.fileID + " result: " + result);
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
}
