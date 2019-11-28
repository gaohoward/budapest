package org.apache.activemq.artemis.budapest.data.store;

import org.apache.activemq.artemis.budapest.iterator.ByteType;
import org.apache.activemq.artemis.budapest.iterator.JournalFile;

import java.io.File;
import java.io.IOException;

public class AbstractJournalFile extends JournalFile
{

   public AbstractJournalFile(File file) throws IOException
   {
      super(file);
   }

   @Override
   public int compareTo(JournalFile o)
   {
      return 0;
   }

   @Override
   protected long getFileHeaderLength()
   {
      return 0;
   }

   @Override
   public ByteType ignoreLastByte(int arg0)
   {
      return null;
   }

   @Override
   protected void init() throws IOException
   {
   }

}
