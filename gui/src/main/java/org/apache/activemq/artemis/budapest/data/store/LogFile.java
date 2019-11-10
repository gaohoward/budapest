package org.apache.activemq.artemis.budapest.data.store;

import org.apache.activemq.artemis.budapest.iterator.ByteType;
import org.apache.activemq.artemis.budapest.iterator.JournalFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LogFile extends JournalFile
{
   private static final Logger logger = LoggerFactory.getLogger(LogFile.class);

   protected List<LogFile> resolved =  new ArrayList<LogFile>();

   public LogFile(File f) throws IOException
   {
      super(f);
   }

   public boolean isSingle()
   {
      return true;
   }

   protected void init() throws IOException
   {
   }

   protected long getFileHeaderLength()
   {
      return 0L;
   }

   public ByteType ignoreLastByte(int theByte)
   {
      throw new IllegalStateException("No byte should be ignored!");
   }

   public int compareTo(JournalFile file)
   {
      return 0;
   }

   public void open(String mode) throws IOException
   {
      super.open(mode);
   }

   public String toString()
   {
      return file.getAbsolutePath();
   }

   public String getShortName()
   {
      String path = file.getAbsolutePath();
      int index = path.lastIndexOf("/");
      if (index != -1)
      {
         path = path.substring(index + 1);
      }
      return path;
   }

   public String getName()
   {
      return getPath();
   }

   public String getPath()
   {
      return file.getAbsolutePath();
   }

   public String getKey()
   {
      return "S" + getName();
   }

   public String getBaseDir()
   {
      throw new IllegalStateException("Single LogFile doesn't have basedir!");
   }

   public boolean isRecursive()
   {
      throw new IllegalStateException("Single LogFile has no recursive attribute!");
   }

   public String getFilter()
   {
      throw new IllegalStateException("Single LogFile has no recursive attribute!");
   }

   public void delete() throws IOException
   {
      this.close();
      file.delete();
   }

   public LogFile cloneLog() throws IOException
   {
      return new LogFile(this.file);
   }

   //must use clone
   public List<LogFile> resolve() throws IOException
   {
      resolved.clear();
      LogFile clone = cloneLog();
      resolved.add(clone);
      return resolved;
   }
}
