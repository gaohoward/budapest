package org.jboss.budapest.data.journal;

import org.apache.activemq.artemis.core.journal.impl.JournalImpl;
import org.jboss.budapest.data.repository.DataStoreInputStream;

import java.io.IOException;

public class JournalPrepareRecord extends TxRecordBase {

   private int txCheckNo;

   //redundant: txExtraData.length
   private int txExtraSize;
   private byte[] txExtraData;

   public JournalPrepareRecord() {
      super(JournalImpl.PREPARE_RECORD);
   }

   @Override
   public void decodeContent(DataStoreInputStream stream) throws IOException {
      super.decodeContent(stream);
      txCheckNo = stream.readInt();
      txExtraSize = stream.readInt();
      txExtraData = new byte[txExtraSize];
      stream.read(txExtraData);
   }
}
