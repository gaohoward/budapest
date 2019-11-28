package org.apache.activemq.artemis.budapest.data.decoder;

import org.apache.activemq.artemis.budapest.data.record.BinaryDataRecord;
import org.apache.activemq.artemis.budapest.iterator.Bookmark;
import org.apache.activemq.artemis.budapest.iterator.DecodeError;
import org.apache.activemq.artemis.budapest.iterator.JournalFile;
import org.apache.activemq.artemis.budapest.iterator.JournalInputStream;
import org.apache.activemq.artemis.budapest.iterator.JournalRecordDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class BinaryRecordDecoder implements JournalRecordDecoder<BinaryDataRecord>
{
   private static final Logger logger = LoggerFactory.getLogger(BinaryRecordDecoder.class);

   @Override
   public BinaryDataRecord decode(JournalInputStream stream) throws DecodeError
   {
      //don't use any buffered reader!
      try
      {
         Bookmark position = stream.getCurrentPosition();
         JournalFile currentFile = stream.getCurrentFile();

         final byte[] data = new byte[32];
         int n = stream.read(data);

         if (n == -1)
         {
            logger.debug("reach the bottom of a file");
            return null;
         }

         BinaryDataRecord record = new BinaryDataRecord(currentFile, position, data, n);
         return record;
      }
      catch (IOException e)
      {
         logger.error("decoding error", e);
         throw new DecodeError(e.getMessage());
      }
   }

}
