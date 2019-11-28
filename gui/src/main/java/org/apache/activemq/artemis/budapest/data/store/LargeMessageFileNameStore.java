package org.apache.activemq.artemis.budapest.data.store;

import org.apache.activemq.artemis.budapest.data.decoder.FileNameDecoder;
import org.apache.activemq.artemis.budapest.data.record.FileNameRecord;
import org.apache.activemq.artemis.budapest.iterator.Bookmark;
import org.apache.activemq.artemis.budapest.iterator.JournalFile;
import org.apache.activemq.artemis.budapest.iterator.JournalRecordDecoder;
import org.apache.activemq.artemis.budapest.iterator.JournalStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class LargeMessageFileNameStore extends JournalStore<FileNameRecord> {
   private static final Logger logger = LoggerFactory.getLogger(LargeMessageFileNameStore.class);
   private List<FileNameRecord> records;
   private File recordFile;

   public LargeMessageFileNameStore(File dir) throws IOException {
      super(dir);
   }

   @Override
   public JournalRecordDecoder<FileNameRecord> getJournalRecordDecoder() {
      return new FileNameDecoder(this);
   }

   @Override
   protected void load() throws IOException {
      //records init here. because load() is called from
      //super constructor, records will be null
      //even if you init it in the member declaration.
      records = new ArrayList<>();
      recordFile = File.createTempFile("lmsg", ".tmp");
      recordFile.deleteOnExit();
      File[] lmFiles = this.journalDir.listFiles(new FilenameFilter() {

         @Override
         public boolean accept(File dir, String name) {
            return name.endsWith(".msg") || name.endsWith(".tmp") || name.endsWith(".sync");
         }

      });
      if (lmFiles != null && lmFiles.length > 0) {
         try (RandomAccessFile handle = new RandomAccessFile(recordFile, "rw")) {
            try (FileChannel channel = handle.getChannel()) {
               ByteBuffer buffer = ByteBuffer.allocateDirect(lmFiles.length * 4);
               for (int i = 0; i < lmFiles.length; i++) {
                  records.add(new FileNameRecord(loadJournalFile(lmFiles[i]), new Bookmark(0, i * 4)));
                  buffer.putInt(i);
               }
               buffer.rewind();
               channel.write(buffer);
            }
         }
         this.journalFiles.add(loadJournalFile(recordFile));
      }
   }

   @Override
   public JournalFile loadJournalFile(File file) throws IOException {
      return new AbstractJournalFile(file);
   }

   @Override
   public boolean isJournalFile(File arg0, String arg1) {
      // TODO Auto-generated method stub
      return false;
   }

   public FileNameRecord getRecord(int index) {
      return records.get(index);
   }
}