package org.jboss.budapest.data.journal;

import org.apache.activemq.artemis.core.journal.impl.JournalImpl;
import org.jboss.budapest.data.repository.DataStoreInputStream;

import java.io.IOException;

public class JournalTxAddRecord extends TxAddRecordBase {

   public JournalTxAddRecord() {
      super(JournalImpl.ADD_RECORD_TX);
   }
}
