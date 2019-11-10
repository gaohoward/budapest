package org.apache.activemq.artemis.budapest.data.record.journal;

import org.apache.activemq.artemis.budapest.data.store.JournalDataFile;
import org.apache.activemq.artemis.budapest.iterator.Bookmark;
import org.apache.activemq.artemis.core.journal.impl.JournalImpl;

public class JournalRollbackRecord extends TxRecordBase {

   public JournalRollbackRecord(JournalDataFile currentFile, Bookmark position) {
      super(JournalImpl.ROLLBACK_RECORD, currentFile, position);
   }

   @Override
   public String typeStr()
   {
      return "ROLLBACK";
   }

   @Override
   public String detailStr()
   {
      return "TxID: " + txId;
   }
}

