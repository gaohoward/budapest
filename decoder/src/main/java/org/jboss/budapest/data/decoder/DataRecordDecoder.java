package org.jboss.budapest.data.decoder;

import org.jboss.budapest.data.repository.DataStoreInputStream;

import java.io.IOException;
import java.io.InputStream;

public abstract class DataRecordDecoder<T extends DataRecord> {

   protected DataStoreInputStream stream;

   public DataRecordDecoder(DataStoreInputStream stream) {
      this.stream = stream;
   }

   public abstract T decode() throws DecodeError, IOException;
}
