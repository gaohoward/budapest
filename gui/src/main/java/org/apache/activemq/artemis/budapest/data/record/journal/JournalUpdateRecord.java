package org.apache.activemq.artemis.budapest.data.record.journal;

import org.apache.activemq.artemis.budapest.data.store.JournalDataFile;
import org.apache.activemq.artemis.budapest.iterator.Bookmark;
import org.apache.activemq.artemis.core.journal.impl.JournalImpl;

public class JournalUpdateRecord extends JournalAddRecordBase {

   public JournalUpdateRecord(JournalDataFile currentFile, Bookmark position) {
      super(JournalImpl.UPDATE_RECORD, currentFile, position);
   }

   @Override
   public String typeStr()
   {
      return "UPDATE";
   }

   @Override
   public String detailStr()
   {
      return baseBody.toString();
   }
}
