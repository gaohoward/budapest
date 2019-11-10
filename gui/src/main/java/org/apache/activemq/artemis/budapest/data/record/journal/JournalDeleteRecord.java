package org.apache.activemq.artemis.budapest.data.record.journal;

import org.apache.activemq.artemis.budapest.data.store.JournalDataFile;
import org.apache.activemq.artemis.budapest.iterator.Bookmark;
import org.apache.activemq.artemis.budapest.iterator.JournalInputStream;
import org.apache.activemq.artemis.core.journal.impl.JournalImpl;

import java.io.IOException;

public class JournalDeleteRecord extends JournalDataRecord {

   private long recordId;

   public JournalDeleteRecord(JournalDataFile currentFile, Bookmark position) {
      super(JournalImpl.DELETE_RECORD, currentFile, position);
   }

   @Override
   public void decodeContent(JournalInputStream stream) throws IOException {
      super.decodeContent(stream);
      recordId = stream.readLong();
   }

   @Override
   public String typeStr()
   {
      return "DELETE";
   }

   @Override
   public String detailStr()
   {
      return "RecID: " + recordId;
   }
}
