package org.apache.activemq.artemis.budapest.data.record;

import org.apache.activemq.artemis.budapest.iterator.Bookmark;
import org.apache.activemq.artemis.budapest.iterator.JournalFile;

import java.io.File;

public class FileNameRecord extends DataRecord
{
   public static final String OP_SHOW_CONTENTS = "Show Contents...";

   public FileNameRecord(JournalFile file, Bookmark position)
   {
      super(file, position);
   }

   @Override
   public String getContent()
   {
      return "Large file: " + this.getSourceFile().getFile().getName();
   }

   public File getFile()
   {
      return this.getSourceFile().getFile();
   }

   public String[] getExtraOps()
   {
      return new String[] {OP_SHOW_CONTENTS};
   }
}
