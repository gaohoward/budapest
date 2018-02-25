package org.jboss.budapest.data.journal;

import org.apache.activemq.artemis.core.journal.impl.JournalImpl;
import org.jboss.budapest.data.repository.DataStoreInputStream;

import java.io.IOException;

public class JournalTxDeleteRecord extends TxRecordBase {

   private long recordId;

   //redundant: body.length
   private int bodySize;
   private byte[] body;

   public JournalTxDeleteRecord() {
      super(JournalImpl.DELETE_RECORD_TX);
   }

   @Override
   public void decodeContent(DataStoreInputStream stream) throws IOException {
      super.decodeContent(stream);
      recordId = stream.readLong();
      bodySize = stream.readInt();
      body = new byte[bodySize];
      stream.read(body);
   }
}
