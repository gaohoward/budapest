package org.apache.activemq.artemis.budapest.data.record.journal;

import org.apache.activemq.artemis.budapest.data.store.JournalDataFile;
import org.apache.activemq.artemis.budapest.iterator.Bookmark;
import org.apache.activemq.artemis.budapest.iterator.JournalInputStream;
import org.apache.activemq.artemis.core.journal.impl.JournalImpl;

import java.io.IOException;

public class JournalCommitRecord extends TxRecordBase {

   private int txCheckNo;

   public JournalCommitRecord(JournalDataFile currentFile, Bookmark position) {
      super(JournalImpl.COMMIT_RECORD, currentFile, position);
   }

   @Override
   public void decodeContent(JournalInputStream stream) throws IOException {
      super.decodeContent(stream);
      txCheckNo = stream.readInt();
   }

   @Override
   public String typeStr()
   {
      return "COMMIT";
   }

   @Override
   public String detailStr()
   {
      return "TxID: " + this.txId + " txCheckNo: " + txCheckNo;
   }

}
