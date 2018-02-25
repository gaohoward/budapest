package org.jboss.budapest.data.journal;

import org.jboss.budapest.data.repository.DataStoreInputStream;

import java.io.IOException;

public abstract class TxRecordBase extends JournalDataRecord {

   protected long txId;

   public TxRecordBase(byte type) {
      super(type);
   }

   @Override
   public void decodeContent(DataStoreInputStream stream) throws IOException {
      txId = stream.readLong();
   }
}
