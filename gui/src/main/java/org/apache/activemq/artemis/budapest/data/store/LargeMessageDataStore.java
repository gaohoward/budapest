package org.apache.activemq.artemis.budapest.data.store;

import org.apache.activemq.artemis.budapest.iterator.JournalFile;

import java.io.File;
import java.io.IOException;

//each store only holds one large file
public class LargeMessageDataStore extends BinaryDataStore
{
   public LargeMessageDataStore(File journalDir) throws IOException
   {
      //.tmp for non-durable, .sync for replication sync
      super(journalDir, ".msg", ".tmp", ".sync");
   }

   //sets the file
   public void setFile(JournalFile largeFile)
   {
      if (journalFiles.size() > 0) throw new IllegalStateException("large file already set");
      if (!isJournalFile(journalDir, largeFile.getFile().getName()))
      {
         throw new IllegalStateException("not a large file " + largeFile.getFile().getName());
      }
      this.journalFiles.add(largeFile);
   }

}
