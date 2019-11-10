package org.apache.activemq.artemis.budapest.data.record.journal;

import org.apache.activemq.artemis.budapest.data.record.DataRecord;
import org.apache.activemq.artemis.budapest.data.store.JournalDataFile;
import org.apache.activemq.artemis.budapest.iterator.Bookmark;
import org.apache.activemq.artemis.budapest.iterator.JournalInputStream;

import java.io.IOException;

public abstract class JournalDataRecord extends DataRecord {

   //defined in JournalImpl
   protected byte type;
   protected int fileRecordId;
   protected byte compactCount;

   //total size of record, including checkSum itself.
   protected int checkSum;


   public JournalDataRecord(byte type, JournalDataFile currentFile, Bookmark position) {
      super(currentFile, position);
      this.type = type;
   }

   public final void decode(JournalInputStream stream) throws IOException {
      fileRecordId = stream.readInt();
      compactCount = stream.readByte();
      decodeContent(stream);
      checkSum = stream.readInt();
   }

   protected void decodeContent(JournalInputStream stream) throws IOException {
   }

   @Override
   public String getContent()
   {
      if (record == null)
      {
         record = "[" + typeStr() + "](" + type + "): " + detailStr() + " (file id: " + fileRecordId + " compactCount: " + compactCount + ")";
      }
      return record;
   }

   public abstract String typeStr();
   public abstract String detailStr();
}
