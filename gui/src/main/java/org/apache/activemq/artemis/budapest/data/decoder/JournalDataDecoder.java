package org.apache.activemq.artemis.budapest.data.decoder;

import org.apache.activemq.artemis.budapest.data.record.journal.JournalDataRecord;
import org.apache.activemq.artemis.budapest.data.record.journal.JournalAddRecord;
import org.apache.activemq.artemis.budapest.data.record.journal.JournalCommitRecord;
import org.apache.activemq.artemis.budapest.data.record.journal.JournalDeleteRecord;
import org.apache.activemq.artemis.budapest.data.record.journal.JournalPrepareRecord;
import org.apache.activemq.artemis.budapest.data.record.journal.JournalRollbackRecord;
import org.apache.activemq.artemis.budapest.data.record.journal.JournalTxAddRecord;
import org.apache.activemq.artemis.budapest.data.record.journal.JournalTxDeleteRecord;
import org.apache.activemq.artemis.budapest.data.record.journal.JournalTxUpdateRecord;
import org.apache.activemq.artemis.budapest.data.record.journal.JournalUpdateRecord;
import org.apache.activemq.artemis.budapest.data.store.JournalDataFile;
import org.apache.activemq.artemis.budapest.iterator.Bookmark;
import org.apache.activemq.artemis.budapest.iterator.DecodeError;
import org.apache.activemq.artemis.budapest.iterator.JournalInputStream;
import org.apache.activemq.artemis.budapest.iterator.JournalRecordDecoder;
import org.apache.activemq.artemis.core.journal.impl.JournalImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.io.IOException;

/**
 * A common util to decode journals 1. it can decode raw journal records 2. in
 * case of corrupted data, it can provide the position of fault
 */
public class JournalDataDecoder implements JournalRecordDecoder<JournalDataRecord>
{
   private static final Logger logger = LoggerFactory.getLogger(JournalDataDecoder.class);

   @Override
   //JournalImpl.readJournalFile, but we don't use callbacks and we don't ignore
   //any records
   public JournalDataRecord decode(JournalInputStream stream) throws DecodeError {
      try {
         Bookmark position = stream.getCurrentPosition();
         JournalDataFile currentFile = (JournalDataFile) stream.getCurrentFile();

         byte type = stream.readByte();

         while (type == 0 || (type < JournalImpl.ADD_RECORD || type > JournalImpl.ROLLBACK_RECORD))
         {
            //what could result in type being non zero?
            //taking a step back how come this "hole" happen in journal file?
            type = stream.readByte();
         }
         JournalDataRecord record = createRecord(type, currentFile, position);
         record.decode(stream);
         return record;
      } catch (EOFException eof) {
         return null;
      }
      catch (IOException e)
      {
         throw new DecodeError(e.getMessage());
      }
   }

   private JournalDataRecord createRecord(byte type, JournalDataFile currentFile, Bookmark position)
   {
      switch (type)
      {
         case JournalImpl.ADD_RECORD:
            return new JournalAddRecord(currentFile, position);
         case JournalImpl.COMMIT_RECORD:
            return new JournalCommitRecord(currentFile, position);
         case JournalImpl.ADD_RECORD_TX:
            return new JournalTxAddRecord(currentFile, position);
         case JournalImpl.DELETE_RECORD:
            return new JournalDeleteRecord(currentFile, position);
         case JournalImpl.DELETE_RECORD_TX:
            return new JournalTxDeleteRecord(currentFile, position);
         case JournalImpl.PREPARE_RECORD:
            return new JournalPrepareRecord(currentFile, position);
         case JournalImpl.ROLLBACK_RECORD:
            return new JournalRollbackRecord(currentFile, position);
         case JournalImpl.UPDATE_RECORD:
            return new JournalUpdateRecord(currentFile, position);
         case JournalImpl.UPDATE_RECORD_TX:
            return new JournalTxUpdateRecord(currentFile, position);
         default:
            throw new IllegalArgumentException(
                     "Unknown record type: " + type + " at " + position + " currentFile: " + currentFile.getFile().getAbsolutePath());
      }
   }

}
