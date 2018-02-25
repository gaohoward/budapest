package org.jboss.budapest.data.journal;

import org.apache.activemq.artemis.core.journal.impl.JournalImpl;
import org.jboss.budapest.data.repository.DataStoreInputStream;

import java.io.IOException;

public class JournalCommitRecord extends TxRecordBase {

   private int txCheckNo;

   public JournalCommitRecord() {
      super(JournalImpl.COMMIT_RECORD);
   }

   @Override
   public void decodeContent(DataStoreInputStream stream) throws IOException {
      super.decodeContent(stream);
      txCheckNo = stream.readInt();
   }

}
