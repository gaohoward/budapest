package org.apache.activemq.artemis.budapest.data.record;

import org.apache.activemq.artemis.budapest.iterator.Bookmark;
import org.apache.activemq.artemis.budapest.iterator.JournalFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataRecord
{
   protected static final Logger logger = LoggerFactory.getLogger(DataRecord.class);

   private static final int MAX_DISPLAY_LEN = 2048;
   protected String record;
   private final JournalFile sourceFile;
   private final Bookmark position;
   private String displayStr;
   private long lineNumber = -1;

   public DataRecord(JournalFile physicalFile, Bookmark position)
   {
      this(null, physicalFile, position);
   }

   public DataRecord(String record, JournalFile physicalFile, Bookmark position)
   {
      this.record = record;
      this.sourceFile = physicalFile;
      this.position = position;
   }

   public JournalFile getSourceFile()
   {
      return this.sourceFile;
   }

   public boolean match(String key)
   {
      String source = toString().toLowerCase();
      String target = key.toLowerCase();
      return source.contains(target);
   }

   public boolean isEmpty()
   {
      return toString().trim().equals("");
   }

   public long getParentLineNumber()
   {
      String record = getContent();
      long result = -1;
      if (record.startsWith("["))
      {
         int inx = record.indexOf("]");
         if (inx != -1)
         {
            String ln = record.substring(1, inx);
            try
            {
               result = Long.valueOf(ln);
            }
            catch (Exception e)
            {
               logger.error("error get line number", e);
               result = -1;
            }
         }
      }
      return result;
   }

   public Bookmark position()
   {
      return position;
   }

   public String getContent()
   {
      return record;
   }

   //limit the length to 2048
   public String getDisplayString()
   {
      if (displayStr == null)
      {
         String record = getContent();
         if (record.length() > MAX_DISPLAY_LEN) {
            String part = record.substring(0, MAX_DISPLAY_LEN);
            displayStr = part + "...";
         }
         else
         {
            displayStr = record;
         }
      }
      return displayStr;
   }

   public long getLineNumber()
   {
      return lineNumber;
   }

   public DataRecord setLineNumber(long num)
   {
      lineNumber = num;
      return this;
   }

   public String getPosition()
   {
      return this.sourceFile.getFile().getName() + " at " + this.position;
   }

   public String[] getExtraOps()
   {
      return null;
   }
}
