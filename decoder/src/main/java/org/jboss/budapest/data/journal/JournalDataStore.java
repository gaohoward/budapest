package org.jboss.budapest.data.journal;

import org.apache.activemq.artemis.api.core.ActiveMQBuffer;
import org.apache.activemq.artemis.api.core.ActiveMQBuffers;
import org.apache.activemq.artemis.api.core.Pair;
import org.jboss.budapest.data.decoder.DataRecord;
import org.jboss.budapest.data.decoder.DataRecordDecoder;
import org.jboss.budapest.data.iterator.DataRecordIterator;
import org.jboss.budapest.data.repository.DataFile;
import org.jboss.budapest.data.repository.DataFileStore;
import org.jboss.budapest.data.repository.DataStoreInputStream;
import org.jboss.budapest.data.repository.PlainDataFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.*;

/**
 * The store parses the journal dir and
 * sorted the journal files.
 */
public class JournalDataStore extends DataFileStore<JournalDataFile> {

   private static final Logger logger = LoggerFactory.getLogger(JournalDataStore.class);

   private JournalDataDecoder decoder = null;
   private File journalDir = null;


   public JournalDataStore(File journalDir) throws IOException {
      super(journalDir);
   }

   //useful for parsing only one or a few fixed journal file.
   public JournalDataStore(JournalDataFile... pFiles) {
      super(pFiles);
   }

   @Override
   //for message journals the prefix is ‘activemq-data’ and the extension(sufix) is ‘amq’
   protected boolean isValidFile(File dir, String name) {
      return name.endsWith(".amq");
   }

   @Override
   protected JournalDataFile createFile(File f) throws IOException {
      return new JournalDataFile(f);
   }

   @Override
   //ref: JournalImpl.load()
   protected void collectDataFiles() throws IOException {
      if (!dataDir.exists()) {
         throw new IllegalArgumentException("data dir not exist: " + dataDir.getAbsolutePath());
      }
      System.out.println("data dir is: " + dataDir.getAbsolutePath());
      journalDir = new File(dataDir, "journal");

      String controlFileName = "journal-rename-control.ctr";

      //files to be deleted at loading. we can't delete them
      //as a tool. but we need to exclude them.
      Set<String> deleteFiles = new HashSet<>();

      //files labeled as new (*.cmp). At loading time
      //.cmp will be tripped off the names.
      //we need to include them
      Set<String> newFiles = new HashSet<>();

      //rename files. at loading if 'from' exists
      //delete 'to' and rename 'from' to 'to'
      //we need to include those 'from-to' files
      //rule: if there is a 'from', check its 'to' name
      Map<String, String> toFiles = new HashMap<>();

      //load the control file and save unused files
      //journal-rename-control.ctr
      //don't delete any files. The whole data folder
      //must be read-only
      File controlFile = getFile(journalDir, controlFileName);
      if (controlFile != null) {
         //The control file is just a journal file with only
         //one add record.
         JournalDataFile pFile = null;
         try {
            pFile = new JournalDataFile(controlFile);
            DataRecordIterator iter = new DataRecordIterator(new JournalDataStore(pFile));
            JournalAddRecord record = (JournalAddRecord) iter.next();
            ActiveMQBuffer input = ActiveMQBuffers.wrappedBuffer(record.getData());

            int numberDataFiles = input.readInt();

            for (int i = 0; i < numberDataFiles; i++) {
               deleteFiles.add(input.readUTF());
            }

            int numberNewFiles = input.readInt();

            for (int i = 0; i < numberNewFiles; i++) {
               newFiles.add(input.readUTF());
            }

            int numberRenames = input.readInt();
            for (int i = 0; i < numberRenames; i++) {
               String from = input.readUTF();
               String to = input.readUTF();
               if (fileExists(from)) {
                  toFiles.put(from, to);
               }
            }
         }
         catch (IOException e) {
            logger.error("got exception parsing control file.", e);
         }
      }
      File[] files = journalDir.listFiles((dir, name) -> isValidFile(dir, name));
      Set<JournalDataFile> initialJouralSet = new HashSet<>();
      for (File f : files) {
         initialJouralSet.add(new JournalDataFile(f));
      }
      //if it's in deleteFiles, ignore
      for (String rmFile : deleteFiles) {
         initialJouralSet.remove(new JournalDataFile(new File(journalDir, rmFile)));
      }

      //adding newFiles
      for (String newFile : newFiles) {
         initialJouralSet.add(new JournalDataFile(new File(journalDir, newFile)));
      }

      //if it's a 'to' file and is in toFiles, add it
      //e.g. a.amq, if toFiles contains (b.amq -> a.amq)
      //b.amq must exists and added as a.amq, while a.amq will
      //be ignored.
      Iterator<Map.Entry<String, String>> iter = toFiles.entrySet().iterator();
      while (iter.hasNext()) {
         Map.Entry<String, String> entry = iter.next();
         String fromName = entry.getKey();
         JournalDataFile jf = new JournalDataFile(new File(journalDir, fromName));
         if (!initialJouralSet.remove(jf)) {
            throw new IllegalStateException("File doesn't exist: " + fromName);
         }
         jf.setName(new File(journalDir, entry.getValue()));
         initialJouralSet.add(jf);
      }
      this.dataFiles.addAll(initialJouralSet);
      //sort
      for (JournalDataFile df : dataFiles) {
         df.open("r");
         df.close();
      }
      Collections.sort(dataFiles);
      System.out.println("____data files: " + dataFiles);
   }

   private boolean fileExists(String from) {
      return new File(from).exists();
   }

   @Override
   public synchronized DataRecordDecoder getDataRecordDecoder(DataStoreInputStream stream) {
      if (this.decoder == null) {
         this.decoder = new JournalDataDecoder(stream);
      }
      return decoder;
   }

   @Override
   public void reset() {
      int size = size();
      for (int i = 0; i < size; i++)
      {
         JournalDataFile jf = this.getDataFile(i);
         try
         {
            jf.close();
         }
         catch (IOException e)
         {
            logger.error("Failed to close " + jf, e);
         }
      }
   }

   @Override
   public void initDecoder(JournalDataFile current) throws IOException {

   }
}
