package org.jboss.budapest.data.decoder;

import org.jboss.budapest.data.repository.DataStoreInputStream;

import java.io.IOException;

public interface DataRecord {

   void decode(DataStoreInputStream stream) throws IOException;

}
