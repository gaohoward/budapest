package org.apache.activemq.artemis.budapest.data.store;

import org.apache.activemq.artemis.budapest.data.decoder.LogRecordDecoder;
import org.apache.activemq.artemis.budapest.data.record.DataRecord;
import org.apache.activemq.artemis.budapest.iterator.JournalFile;
import org.apache.activemq.artemis.budapest.iterator.JournalRecordDecoder;
import org.apache.activemq.artemis.budapest.iterator.JournalStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Each log store manages a set of log files in one dir.
 * log files are 'manually' managed, meaning that we dont
 * put any limitations on the file names.
 * log files must be line-based text file.
 */
public class LogStore extends JournalStore<DataRecord>
{
   private static final Logger logger = LoggerFactory.getLogger(LogStore.class);

   private Map<String, LogFile> logFiles = new LinkedHashMap<>();
   private List<LogFile> fileListCopy;
   private LogRecordDecoder decoder = new LogRecordDecoder();

   public LogStore() throws IOException
   {
      super(new File("."));
   }

   public void addLogFile(LogFile logFile) throws IOException
   {
      List<LogFile> singleLogFiles = logFile.resolve();
      for (LogFile lf : singleLogFiles)
      {
         logFiles.put(lf.getName(), lf);
      }
      fileListCopy = null;
   }

   public void addLogFiles(List<LogFile> logFiles) throws IOException
   {
      for (LogFile lf : logFiles)
      {
         addLogFile(lf);
      }
      fileListCopy = null;
   }

   //we are not really dir based, so
   @Override
   protected void load() throws IOException
   {
   }

   public JournalRecordDecoder<DataRecord> getJournalRecordDecoder()
   {
      return decoder;
   }

   public boolean isJournalFile(File dir, String name)
   {
      return logFiles.containsKey(name);
   }

   public JournalFile loadJournalFile(File file) throws IOException
   {
      return logFiles.get(file.getName());
   }

   @Override
   public int size()
   {
      return logFiles.size();
   }

   @Override
   public JournalFile getJournalFile(final int fileIndex)
   {
      if (fileIndex > -1 && fileIndex < logFiles.size())
      {
         if (fileListCopy == null)
         {
            Iterator<LogFile> iter = logFiles.values().iterator();
            fileListCopy = new ArrayList<>();
            while (iter.hasNext())
            {
               fileListCopy.add((LogFile)iter.next());
            }
         }

         JournalFile file = fileListCopy.get(fileIndex);
         return file;
      }
      throw new IllegalStateException("Invalid index: " + fileIndex + " for logStore: " + logFiles);
   }

   @Override
   public void initDecoder(JournalFile journal) throws IOException
   {
      long curPos = journal.position();
      try
      {
         if (curPos > 0)
         {
            journal.position(curPos - 1);
            ByteBuffer buffer = ByteBuffer.allocate(2);
            if (journal.read(buffer) != 2)
            {
               throw new IllegalStateException("Error reading the byte from " + journal + " original position " + curPos);
            }
            buffer.flip();
            byte b1 = buffer.get();
            byte b2 = buffer.get();
            decoder.initFlag(b1, b2);
         }
      }
      finally
      {
         //restore position
         journal.position(curPos);
      }

   }

}
