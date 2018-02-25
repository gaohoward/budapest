package org.jboss.budapest.data.journal;

import org.jboss.budapest.data.repository.DataStoreInputStream;

import java.io.IOException;

public class AddRecordBaseBody {
   long recordId;
   //redundant, it's the body.length, let's just keep it there
   //to make the data structure complete.
   int bodySize;
   byte uType;
   byte[] body;

   public void decode(DataStoreInputStream stream) throws IOException {
      recordId = stream.readLong();
      bodySize = stream.readInt();
      uType = stream.readByte();
      body = new byte[bodySize];
      stream.read(body);
   }
}
