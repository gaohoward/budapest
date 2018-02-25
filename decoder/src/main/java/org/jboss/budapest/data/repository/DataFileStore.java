package org.jboss.budapest.data.repository;

import org.jboss.budapest.data.decoder.DataRecord;
import org.jboss.budapest.data.decoder.DataRecordDecoder;
import org.jboss.budapest.data.journal.JournalDataFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * manage data files for artemis
 * (like journal, page ...)
 */
public abstract class DataFileStore <T extends DataFile> {
   private static final Logger logger = LoggerFactory.getLogger(DataFileStore.class);
   protected File dataDir;
   protected List<T> dataFiles = new ArrayList<>();
   protected boolean loaded = false;

   public DataFileStore(File dataDir) throws IOException
   {
      this.dataDir = dataDir;
      this.load();
   }

   public DataFileStore(T[] pFiles) {
      for (T f : pFiles) {
         dataFiles.add(f);
      }
      loaded = true;
   }

   /**
    * is the file a valid data file?
    * @param dir The file location
    * @param name Name of the file
    * @return
    */
   protected abstract boolean isValidFile(File dir, String name);

   protected abstract T createFile(File f) throws IOException;

   //load the data file list
   protected void load() throws IOException
   {
      if (loaded) {
         return;
      }
      collectDataFiles();
   }

   public T getDataFile(int fileIndex)
   {
      return dataFiles.get(fileIndex);
   }

   public int size()
   {
      return dataFiles.size();
   }

   protected File getFile(File dir, String fname) {
      File result = new File(dir, fname);
      if (result.exists()) {
         return result;
      }
      return null;
   }

   protected void collectDataFiles() throws IOException {
      File[] files = dataDir.listFiles((dir, name) -> isValidFile(dir, name));
      for (File f : files)
      {
         T dataFile = createFile(f);
         dataFiles.add(dataFile);
      }
   }

   public abstract DataRecordDecoder<? extends DataRecord> getDataRecordDecoder(DataStoreInputStream stream);

   public abstract void reset();

   //in case of the decoder needs to know a new file is opened
   //for decoding. (maybe using a event listener instead?)
   public abstract void initDecoder(T current) throws IOException;
}
