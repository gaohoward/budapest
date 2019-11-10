package org.apache.activemq.artemis.budapest.data.record.journal;

import org.apache.activemq.artemis.budapest.data.store.JournalDataFile;
import org.apache.activemq.artemis.budapest.iterator.Bookmark;
import org.apache.activemq.artemis.budapest.iterator.JournalInputStream;

import java.io.IOException;

public abstract class TxAddRecordBase extends TxRecordBase {

   protected AddRecordBaseBody baseBody;

   public TxAddRecordBase(byte type, JournalDataFile currentFile, Bookmark position) {
      super(type, currentFile, position);
      baseBody = new AddRecordBaseBody();
   }

   @Override
   public void decodeContent(JournalInputStream stream) throws IOException {
      super.decodeContent(stream);
      baseBody.decode(stream);
   }

   @Override
   public String detailStr()
   {
      return "txID: " + txId + " " + baseBody.toString();
   }
}
