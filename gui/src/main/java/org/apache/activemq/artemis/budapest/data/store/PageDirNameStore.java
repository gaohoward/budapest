package org.apache.activemq.artemis.budapest.data.store;

import org.apache.activemq.artemis.budapest.data.decoder.DirNameDecoder;
import org.apache.activemq.artemis.budapest.data.record.DirNameRecord;
import org.apache.activemq.artemis.budapest.iterator.Bookmark;
import org.apache.activemq.artemis.budapest.iterator.JournalFile;
import org.apache.activemq.artemis.budapest.iterator.JournalRecordDecoder;
import org.apache.activemq.artemis.budapest.iterator.JournalStore;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class PageDirNameStore extends JournalStore<DirNameRecord>
{
   private List<DirNameRecord> records;
   private File recordFile;

   public PageDirNameStore(File dir) throws IOException
   {
      super(dir);
   }

   @Override
   public JournalRecordDecoder<DirNameRecord> getJournalRecordDecoder()
   {
      return new DirNameDecoder(this);
   }

   @Override
   protected void load() throws IOException
   {
      //records init here. because load() is called from
      //super constructor, records will be null
      //even if you init it in the member declaration.
      records = new ArrayList<>();
      recordFile = File.createTempFile("pagedirs", ".tmp");
      recordFile.deleteOnExit();
      File[] pgDirs = this.journalDir.listFiles(new FilenameFilter() {

         @Override
         public boolean accept(File dir, String name)
         {
            return dir.isDirectory() && name.length() == 36;
         }

      });
      if (pgDirs != null && pgDirs.length > 0)
      {
         try (RandomAccessFile handle = new RandomAccessFile(recordFile, "rw"))
         {
            try (FileChannel channel = handle.getChannel())
            {
               ByteBuffer buffer = ByteBuffer.allocateDirect(pgDirs.length * 4);
               for (int i = 0; i < pgDirs.length; i++)
               {
                  records.add(new DirNameRecord(loadJournalFile(pgDirs[i]), new Bookmark(0, i*4)));
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
   public JournalFile loadJournalFile(File file) throws IOException
   {
      return new AbstractJournalFile(file);
   }

   @Override
   public boolean isJournalFile(File arg0, String arg1)
   {
      // TODO Auto-generated method stub
      return false;
   }

   public DirNameRecord getRecord(int index)
   {
      return records.get(index);
   }

}
