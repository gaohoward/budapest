package org.apache.activemq.artemis.budapest.data.decoder;

import org.apache.activemq.artemis.budapest.data.record.FileNameRecord;
import org.apache.activemq.artemis.budapest.data.store.LargeMessageFileNameStore;
import org.apache.activemq.artemis.budapest.iterator.DecodeError;
import org.apache.activemq.artemis.budapest.iterator.JournalInputStream;
import org.apache.activemq.artemis.budapest.iterator.JournalRecordDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.io.IOException;

public class FileNameDecoder implements JournalRecordDecoder<FileNameRecord>
{
   private static final Logger logger = LoggerFactory.getLogger(FileNameDecoder.class);

   private LargeMessageFileNameStore store;

   public FileNameDecoder(LargeMessageFileNameStore store)
   {
      this.store = store;
   }

   @Override
   public FileNameRecord decode(JournalInputStream stream) throws DecodeError
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
