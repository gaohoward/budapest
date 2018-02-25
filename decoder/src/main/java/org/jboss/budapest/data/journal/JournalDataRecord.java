package org.jboss.budapest.data.journal;

import org.jboss.budapest.data.decoder.DataRecord;
import org.jboss.budapest.data.repository.DataStoreInputStream;

import java.io.IOException;

public abstract class JournalDataRecord implements DataRecord {

   //defined in JournalImpl
   protected byte type;
   protected int fileRecordId;
   protected byte compactCount;

   //total size of record, including checkSum itself.
   protected int checkSum;


   public JournalDataRecord(byte type) {
      this.type = type;
   }

   @Override
   public final void decode(DataStoreInputStream stream) throws IOException {
      fileRecordId = stream.readInt();
      compactCount = stream.readByte();
      decodeContent(stream);
      checkSum = stream.readInt();
   }

   protected void decodeContent(DataStoreInputStream stream) throws IOException {
   }
}
