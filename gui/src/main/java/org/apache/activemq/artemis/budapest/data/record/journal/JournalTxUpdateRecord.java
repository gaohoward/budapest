package org.apache.activemq.artemis.budapest.data.record.journal;

import org.apache.activemq.artemis.budapest.data.store.JournalDataFile;
import org.apache.activemq.artemis.budapest.iterator.Bookmark;
import org.apache.activemq.artemis.core.journal.impl.JournalImpl;

public class JournalTxUpdateRecord extends TxAddRecordBase {

   public JournalTxUpdateRecord(JournalDataFile currentFile, Bookmark position) {
      super(JournalImpl.UPDATE_RECORD_TX, currentFile, position);
   }

   @Override
   public String typeStr()
   {
      return "TX-UPDATE";
   }
}
