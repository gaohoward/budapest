package org.jboss.budapest.data.journal;

import org.apache.activemq.artemis.core.journal.impl.JournalImpl;
import org.jboss.budapest.data.repository.DataStoreInputStream;

import java.io.IOException;

public class JournalDeleteRecord extends JournalDataRecord {

   private long recordId;

   public JournalDeleteRecord() {
      super(JournalImpl.DELETE_RECORD);
   }

   @Override
   public void decodeContent(DataStoreInputStream stream) throws IOException {
      super.decodeContent(stream);
      recordId = stream.readLong();
   }
}
