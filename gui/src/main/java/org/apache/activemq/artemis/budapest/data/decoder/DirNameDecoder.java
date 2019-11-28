package org.apache.activemq.artemis.budapest.data.decoder;

import org.apache.activemq.artemis.budapest.data.record.DirNameRecord;
import org.apache.activemq.artemis.budapest.data.store.PageDirNameStore;
import org.apache.activemq.artemis.budapest.iterator.DecodeError;
import org.apache.activemq.artemis.budapest.iterator.JournalInputStream;
import org.apache.activemq.artemis.budapest.iterator.JournalRecordDecoder;

import java.io.EOFException;
import java.io.IOException;

public class DirNameDecoder implements JournalRecordDecoder<DirNameRecord>
{
   private PageDirNameStore store;

   public DirNameDecoder(PageDirNameStore store)
   {
      this.store = store;
   }

   @Override
   public DirNameRecord decode(JournalInputStream stream) throws DecodeError
   {
      int index;
      try
      {
         index = stream.readInt();
         return store.getRecord(index);
      }
      catch (EOFException e)
      {
         return null;
      }
      catch (IOException e)
      {
         return null;
      }
   }
}
