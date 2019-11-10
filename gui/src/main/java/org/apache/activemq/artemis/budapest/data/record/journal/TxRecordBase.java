package org.apache.activemq.artemis.budapest.data.record.journal;

import org.apache.activemq.artemis.budapest.data.store.JournalDataFile;
import org.apache.activemq.artemis.budapest.iterator.Bookmark;
import org.apache.activemq.artemis.budapest.iterator.JournalInputStream;

import java.io.IOException;

public abstract class TxRecordBase extends JournalDataRecord {

   protected long txId;

   public TxRecordBase(byte type, JournalDataFile currentFile, Bookmark position) {
      super(type, currentFile, position);
   }

   @Override
   public void decodeContent(JournalInputStream stream) throws IOException {
      txId = stream.readLong();
   }
}
