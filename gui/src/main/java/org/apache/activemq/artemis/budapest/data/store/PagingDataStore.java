package org.apache.activemq.artemis.budapest.data.store;

import org.apache.activemq.artemis.budapest.data.decoder.PageRecordDecoder;
import org.apache.activemq.artemis.budapest.data.record.paging.PageDataRecord;
import org.apache.activemq.artemis.budapest.iterator.JournalFile;
import org.apache.activemq.artemis.budapest.iterator.JournalRecordDecoder;
import org.apache.activemq.artemis.budapest.iterator.JournalStore;

import java.io.File;
import java.io.IOException;

public class PagingDataStore extends JournalStore<PageDataRecord>
{
   // each address has a pagingDataStore
   // page dir is a <uuid>, the address is stored
   // in address.txt under the pagedir.
   // all page files have formats like <pageId<int>>.page
   // the pageID is incrementally ordered.
   public PagingDataStore(File pageDir) throws IOException
   {
      super(pageDir);
   }

   @Override
   public JournalRecordDecoder<PageDataRecord> getJournalRecordDecoder()
   {
      return new PageRecordDecoder();
   }

   @Override
   public boolean isJournalFile(File dir, String name)
   {
      return name.endsWith(".page");
   }

   @Override
   public JournalFile loadJournalFile(File file) throws IOException
   {
      return new AbstractJournalFile(file) {

         @Override
         public int compareTo(JournalFile other)
         {
            String name1 = this.getFile().getName();
            String name2 = other.getFile().getName();

            int myId = Integer.valueOf(name1.substring(0, name1.indexOf(".")));
            int yourId = Integer.valueOf(name2.substring(0, name2.indexOf(".")));

            return myId > yourId ? 1 : (myId < yourId ? -1 : 0);
         }

      };
   }

}
