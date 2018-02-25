package org.jboss.budapest.data.journal;

import org.jboss.budapest.data.repository.DataStoreInputStream;

import java.io.IOException;

public abstract class JournalAddRecordBase extends JournalDataRecord {

   protected AddRecordBaseBody baseBody;

   public JournalAddRecordBase(byte type) {
      super(type);
      baseBody = new AddRecordBaseBody();
   }

   @Override
   public void decodeContent(DataStoreInputStream stream) throws IOException {
      baseBody.decode(stream);
   }

   public byte[] getData() {
      return baseBody.body;
   }
}
