package org.apache.activemq.artemis.budapest.iterator;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Load journal files from a specifc path.
 * 
 * @author howard
 *
 */
public abstract class JournalStore<T>
{
   private static final Logger logger = LoggerFactory.getLogger(JournalStore.class);
   protected File journalDir;
   protected List<JournalFile> journalFiles = new ArrayList<JournalFile>();
   
   public JournalStore(File journalDir) throws IOException
   {
      this.journalDir = journalDir;
      this.load();
   }
   
   public JournalStore(JournalFile... files)
   {
     for (JournalFile f : files)
     {
       journalFiles.add(f);
     }
   }

   /**
    * is the file a valid journal file?
    * @param dir The file location
    * @param name Name of the file
    * @return
    */
   public abstract boolean isJournalFile(File dir, String name);

   /**
    * Create the Journal File from the physical file
    * @param file
    * @return
    */
   public abstract JournalFile loadJournalFile(File file) throws IOException;

   //load the journal file list
   protected void load() throws IOException
   {
      File[] files = journalDir.listFiles(new FilenameFilter() {
         @Override
         public boolean accept(File dir, String name)
         {
            return isJournalFile(dir, name);
         }
      });

      for (File f : files)
      {
         JournalFile journal = loadJournalFile(f);
         journalFiles.add(journal);
      }

      //sort
      Collections.sort(journalFiles);
      
      for (int i = 0; i < journalFiles.size(); i++)
      {
         System.out.println("file: " + journalFiles.get(i));
      }
   }

   public JournalFile getJournalFile(int fileIndex)
   {
      return journalFiles.get(fileIndex);
   }
   
   public int size()
   {
      return journalFiles.size();
   }

   public abstract JournalRecordDecoder<T> getJournalRecordDecoder();

   //tell the decoder where to begin, so decoder can do
   //some init before decoding.
   public void initDecoder(JournalFile current) throws IOException
   {
   }

   //this is use to close any resources (journals)
   //in the store.
   public void reset()
   {
      int size = size();
      for (int i = 0; i < size; i++)
      {
        JournalFile jf = this.getJournalFile(i);
        try
        {
          jf.close();
        }
        catch (IOException e)
        {
          logger.error("failed to close {}", jf, e);
        }
      }
   }
}
