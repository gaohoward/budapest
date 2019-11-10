package org.apache.activemq.artemis.pudapest.iterator.test.util;

import java.io.File;
import java.io.IOException;

import org.apache.activemq.artemis.budapest.iterator.JournalFile;
import org.apache.activemq.artemis.budapest.iterator.JournalRecordDecoder;
import org.apache.activemq.artemis.budapest.iterator.JournalStore;

public class SimpleJournalStore extends JournalStore {

   public SimpleJournalStore(File journalDir) throws Exception
   {
      super(journalDir);
   }

   @Override
   public boolean isJournalFile(File dir, String name) {
      return name.endsWith(".jnl");
   }

   @Override
   public JournalFile loadJournalFile(File file) throws IOException {
      SimpleJournalFile journal = new SimpleJournalFile(file);
      journal.open("r");//open and close just for reading header
      journal.close();
      return journal;
   }

   @Override
   public JournalRecordDecoder<?> getJournalRecordDecoder()
   {
      return new SimpleJournalRecordDecoder();
   }

   public static void cleanStoreBaseDir() throws Exception
   {
      String userHome = System.getProperty("user.home");
      File home = new File(userHome);
      if (!home.exists())
      {
         throw new IllegalStateException("User home not exist " + userHome);
      }

      File tmp = new File(home, "tmpJournalStore");
      if (tmp.exists())
      {
         deleteDir(tmp);
      }      
   }

   private static void deleteDir(File dir) throws Exception
   {
      if (dir.isDirectory())
      {
         File[] files = dir.listFiles();
         for (File f : files)
         {
            if (f.isDirectory())
            {
               deleteDir(f);
            }
            else
            {
               f.delete();
            }
         }
      }
      //delete
      dir.delete();
   }

   public static SimpleJournalStore createStore(String name, int fileSize, 
         int preCreateFiles, int numRecords) throws Exception
   {
      String userHome = System.getProperty("user.home");
      File home = new File(userHome);
      if (!home.exists())
      {
         throw new IllegalStateException("User home not exist " + userHome);
      }
      File tmp = new File(home, "tmpJournalStore");
      if (!tmp.exists())
      {
         tmp.mkdir();
      }

      File storeDir = new File(tmp, name);
      if (storeDir.exists())
      {
         throw new IllegalStateException("store dir already exist: " + storeDir);
      }
      
      storeDir.mkdir();
      
      //now files
      for (int i = 0; i < preCreateFiles; i++)
      {
         String fname = "J" + i + ".jnl";
         File journal = new File(storeDir, fname);
         SimpleJournalFile.format(journal, i, fileSize);
      }

      //now write records
      SimpleJournalStore store = new SimpleJournalStore(storeDir);      
      store.populate(numRecords);
      
      return store;
   }

   private SimpleRecord[] populate(int numRecords) throws IOException
   {
      int index = 0;
      SimpleJournalFile journalFile = (SimpleJournalFile) this.getJournalFile(index);
      journalFile.open("rw");
      journalFile.beginWrite();
      
      SimpleJournalRecordDecoder decoder = new SimpleJournalRecordDecoder();

      SimpleRecord[] records = new SimpleRecord[numRecords];
      for (int i = 0; i < numRecords; i++)
      {
         records[i] = decoder.createRandomRecord(i);
      }
      
      for (SimpleRecord r : records)
      {
         while (!journalFile.writeRecord(r))
         {
            index ++;
            if (index >= this.size())
            {
               throw new IllegalStateException("Not enough journal file to hold records");
            }
            journalFile.padding();
            journalFile.close();
            journalFile = (SimpleJournalFile) this.getJournalFile(index);
            journalFile.open("rw");
            journalFile.beginWrite();
         }
      }
      journalFile.close();
      
      return records;
   }

}
