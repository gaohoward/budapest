package org.apache.activemq.artemis.budapest.data.record.journal;

import org.apache.activemq.artemis.budapest.data.store.JournalDataFile;
import org.apache.activemq.artemis.budapest.iterator.Bookmark;
import org.apache.activemq.artemis.core.journal.impl.JournalImpl;

public class JournalTxAddRecord extends TxAddRecordBase {

   public JournalTxAddRecord(JournalDataFile currentFile, Bookmark position) {
      super(JournalImpl.ADD_RECORD_TX, currentFile, position);
   }

   @Override
   public String typeStr()
   {
      return "TX-ADD";
   }
}
