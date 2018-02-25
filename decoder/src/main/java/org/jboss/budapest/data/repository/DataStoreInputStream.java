package org.jboss.budapest.data.repository;

import org.jboss.budapest.data.iterator.Bookmark;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public abstract class DataStoreInputStream extends DataInputStream {

   public DataStoreInputStream(InputStream in) {
      super(in);
   }

   public Bookmark getCurrentPosition() throws IOException {
      return null;
   }

   public abstract File getCurrentFile();
}
