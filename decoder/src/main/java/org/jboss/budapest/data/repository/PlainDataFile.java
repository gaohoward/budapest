package org.jboss.budapest.data.repository;

import java.io.File;
import java.io.IOException;

//plain raw file, used to read bytes from file
public class PlainDataFile extends DataFile<PlainDataFile> {

   public PlainDataFile(File file) throws IOException {
      super(file);
   }

   @Override
   protected void init() throws IOException {
   }

   @Override
   protected long getFileHeaderLength() {
      return 0;
   }

   @Override
   public int compareTo(PlainDataFile o) {
      return 0;
   }
}
