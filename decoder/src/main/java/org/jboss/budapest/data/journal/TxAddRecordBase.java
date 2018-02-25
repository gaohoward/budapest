package org.jboss.budapest.data.journal;

import org.jboss.budapest.data.repository.DataStoreInputStream;

import java.io.IOException;

public class TxAddRecordBase extends TxRecordBase {

   protected AddRecordBaseBody baseBody;

   public TxAddRecordBase(byte type) {
      super(type);
   }

   @Override
   public void decodeContent(DataStoreInputStream stream) throws IOException {
      super.decodeContent(stream);
      baseBody.decode(stream);
   }
}
