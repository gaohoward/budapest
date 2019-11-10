package org.apache.activemq.artemis.budapest.data.record.journal;

import org.apache.activemq.artemis.budapest.iterator.JournalInputStream;

import java.io.IOException;

class AddRecordBaseBody {
   long recordId;
   //redundant, it's the body.length, let's just keep it there
   //to make the data structure complete.
   int bodySize;
   byte uType;
   byte[] body;

   public void decode(JournalInputStream stream) throws IOException {
      recordId = stream.readLong();
      bodySize = stream.readInt();
      uType = stream.readByte();
      body = new byte[bodySize];
      stream.read(body);
   }

   public String toString()
   {
      return "RecID: " + recordId + " UserType: " + uType + " [" + bodySize + " bytes data]";
   }
}
