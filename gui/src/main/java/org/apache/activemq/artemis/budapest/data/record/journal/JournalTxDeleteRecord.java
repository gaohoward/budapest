package org.apache.activemq.artemis.budapest.data.record.journal;

import org.apache.activemq.artemis.budapest.data.store.JournalDataFile;
import org.apache.activemq.artemis.budapest.iterator.Bookmark;
import org.apache.activemq.artemis.budapest.iterator.JournalInputStream;
import org.apache.activemq.artemis.core.journal.impl.JournalImpl;

import java.io.IOException;

public class JournalTxDeleteRecord extends TxRecordBase {

   private long recordId;

   //redundant: body.length
   private int bodySize;
   private byte[] body;

   public JournalTxDeleteRecord(JournalDataFile currentFile, Bookmark position) {
      super(JournalImpl.DELETE_RECORD_TX, currentFile, position);
   }

   @Override
   public void decodeContent(JournalInputStream stream) throws IOException {
      super.decodeContent(stream);
      recordId = stream.readLong();
      bodySize = stream.readInt();
      body = new byte[bodySize];
      stream.read(body);
   }

   @Override
   public String typeStr()
   {
      return "TX-DELETE";
   }

   @Override
   public String detailStr()
   {
      return "txID: " + txId + " RecID: " + recordId + " bodySize: " + bodySize;
   }
}
