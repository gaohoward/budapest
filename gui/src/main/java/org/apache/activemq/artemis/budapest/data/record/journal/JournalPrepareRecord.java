package org.apache.activemq.artemis.budapest.data.record.journal;

import org.apache.activemq.artemis.budapest.data.store.JournalDataFile;
import org.apache.activemq.artemis.budapest.iterator.Bookmark;
import org.apache.activemq.artemis.budapest.iterator.JournalInputStream;
import org.apache.activemq.artemis.core.journal.impl.JournalImpl;

import java.io.IOException;

public class JournalPrepareRecord extends TxRecordBase {

   private int txCheckNo;

   //redundant: txExtraData.length
   private int txExtraSize;
   private byte[] txExtraData;

   public JournalPrepareRecord(JournalDataFile currentFile, Bookmark position) {
      super(JournalImpl.PREPARE_RECORD, currentFile, position);
   }

   @Override
   public void decodeContent(JournalInputStream stream) throws IOException {
      super.decodeContent(stream);
      txCheckNo = stream.readInt();
      txExtraSize = stream.readInt();
      txExtraData = new byte[txExtraSize];
      stream.read(txExtraData);
   }

   @Override
   public String typeStr()
   {
      return "PREPARE";
   }

   @Override
   public String detailStr()
   {
      return "TxID: " + txId + " txExtra: " + txExtraSize + " bytes txCheckNo: " + txCheckNo;
   }
}
