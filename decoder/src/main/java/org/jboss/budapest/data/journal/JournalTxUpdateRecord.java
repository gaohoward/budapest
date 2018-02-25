package org.jboss.budapest.data.journal;

import org.apache.activemq.artemis.core.journal.impl.JournalImpl;
import org.jboss.budapest.data.repository.DataStoreInputStream;

import java.io.IOException;

public class JournalTxUpdateRecord extends TxAddRecordBase {

   public JournalTxUpdateRecord() {
      super(JournalImpl.UPDATE_RECORD_TX);
   }
}
