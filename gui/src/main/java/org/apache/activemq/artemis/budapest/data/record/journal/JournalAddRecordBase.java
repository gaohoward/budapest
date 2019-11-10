package org.apache.activemq.artemis.budapest.data.record.journal;

import org.apache.activemq.artemis.budapest.data.store.JournalDataFile;
import org.apache.activemq.artemis.budapest.iterator.Bookmark;
import org.apache.activemq.artemis.budapest.iterator.JournalInputStream;

import java.io.IOException;

public abstract class JournalAddRecordBase extends JournalDataRecord {

   protected AddRecordBaseBody baseBody;

   public JournalAddRecordBase(byte type, JournalDataFile currentFile, Bookmark position) {
      super(type, currentFile, position);
      baseBody = new AddRecordBaseBody();
   }

   @Override
   public void decodeContent(JournalInputStream stream) throws IOException {
      baseBody.decode(stream);
   }

   public byte[] getData() {
      return baseBody.body;
   }
}
