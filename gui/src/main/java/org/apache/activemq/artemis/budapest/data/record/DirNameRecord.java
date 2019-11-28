package org.apache.activemq.artemis.budapest.data.record;

import org.apache.activemq.artemis.budapest.iterator.Bookmark;
import org.apache.activemq.artemis.budapest.iterator.JournalFile;
import org.apache.activemq.artemis.budapest.util.GeneralUtil;

import java.io.File;

public class DirNameRecord extends DataRecord
{
   public static final String OP_SHOW_CONTENTS = "Show Contents...";
   public static final long MAX_ADDR_LEN = 2048;

   private String address;

   public DirNameRecord(JournalFile file, Bookmark position)
   {
      super(file, position);
      address = GeneralUtil.parseAddress(this.getFile());
   }

   @Override
   public String getContent()
   {
      return "Page Dir: " + this.getSourceFile().getFile().getName() + "(" + address + ")";
   }

   public File getFile()
   {
      return this.getSourceFile().getFile();
   }

   public String[] getExtraOps()
   {
      return new String[] {OP_SHOW_CONTENTS};
   }

   public String getAddress()
   {
      return address;
   }

}
