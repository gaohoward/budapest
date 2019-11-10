package org.apache.activemq.artemis.budapest.data.record.journal;

import org.apache.activemq.artemis.budapest.data.store.JournalDataFile;
import org.apache.activemq.artemis.budapest.iterator.Bookmark;
import org.apache.activemq.artemis.core.journal.impl.JournalImpl;

public class JournalAddRecord extends JournalAddRecordBase {

   public JournalAddRecord(JournalDataFile currentFile, Bookmark position) {
      super(JournalImpl.ADD_RECORD, currentFile, position);
   }

   @Override
   public String typeStr()
   {
      return "ADD";
   }

   @Override
   public String detailStr()
   {
      return baseBody.toString();
   }
}

