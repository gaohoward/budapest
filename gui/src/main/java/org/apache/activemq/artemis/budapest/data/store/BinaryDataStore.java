package org.apache.activemq.artemis.budapest.data.store;

import org.apache.activemq.artemis.budapest.data.decoder.BinaryRecordDecoder;
import org.apache.activemq.artemis.budapest.data.record.BinaryDataRecord;
import org.apache.activemq.artemis.budapest.iterator.JournalFile;
import org.apache.activemq.artemis.budapest.iterator.JournalRecordDecoder;
import org.apache.activemq.artemis.budapest.iterator.JournalStore;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class BinaryDataStore extends JournalStore<BinaryDataRecord>
{
   private Set<String> suffices = new HashSet<>();

   public BinaryDataStore(File journalDir, String... suffix) throws IOException
   {
      super(journalDir);
      if (suffix != null)
      {
         for (String s : suffix)
         {
            suffices.add(s);
         }
      }
   }

   public BinaryDataStore(File singleFile) throws IOException
   {
      super(singleFile.getParentFile());
      this.journalFiles.add(new AbstractJournalFile(singleFile));
   }
   //have to override this. cause it's called from constructor.
   //not a good idea in this case
   protected void load() throws IOException
   {
   }

   @Override
   public boolean isJournalFile(File dir, String name)
   {
      for (String s : suffices)
      {
         if (name.endsWith(s))
         {
            return true;
         }
      }
      return false;
   }

   @Override
   public JournalRecordDecoder<BinaryDataRecord> getJournalRecordDecoder()
   {
      return new BinaryRecordDecoder();
   }

   @Override
   public JournalFile loadJournalFile(File arg0) throws IOException
   {
      return null;
   }

}
