package org.jboss.budapest.data.journal;

import org.apache.activemq.artemis.core.journal.impl.JournalImpl;
import org.jboss.budapest.data.decoder.DataRecordDecoder;
import org.jboss.budapest.data.repository.DataStoreInputStream;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * A common util to decode journals
 * 1. it can decode raw journal records
 * 2. in case of corrupted data, it can provide
 * the position of fault
 */
public class JournalDataDecoder extends DataRecordDecoder<JournalDataRecord> {

   public JournalDataDecoder(DataStoreInputStream stream) {
      super(stream);
   }

   @Override
   //JournalImpl.readJournalFile, but we don't use callbacks
   public JournalDataRecord decode() throws IOException {
      try {
         byte type = stream.readByte();

         while (type == 0) {
            type = stream.readByte();
         }

         JournalDataRecord record = createRecord(type);
         System.out.println("__decoding " + record);
         record.decode(stream);
         return record;
      } catch (EOFException eof) {
         return null;
      }
   }

   private JournalDataRecord createRecord(byte type) {
      switch (type) {
         case JournalImpl.ADD_RECORD:
            return new JournalAddRecord();
         case JournalImpl.COMMIT_RECORD:
            return new JournalCommitRecord();
         case JournalImpl.ADD_RECORD_TX:
            return new JournalTxAddRecord();
         case JournalImpl.DELETE_RECORD:
            return new JournalDeleteRecord();
         case JournalImpl.DELETE_RECORD_TX:
            return new JournalTxDeleteRecord();
         case JournalImpl.PREPARE_RECORD:
            return new JournalPrepareRecord();
         case JournalImpl.ROLLBACK_RECORD:
            return new JournalRollbackRecord();
         case JournalImpl.UPDATE_RECORD:
            return new JournalUpdateRecord();
         case JournalImpl.UPDATE_RECORD_TX:
            return new JournalTxUpdateRecord();
         default:
            throw new IllegalArgumentException("Unknown record type: " + type);
      }
   }
}
