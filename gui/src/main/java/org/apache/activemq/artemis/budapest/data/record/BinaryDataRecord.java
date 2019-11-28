package org.apache.activemq.artemis.budapest.data.record;

import org.apache.activemq.artemis.budapest.iterator.Bookmark;
import org.apache.activemq.artemis.budapest.iterator.JournalFile;
import org.apache.activemq.artemis.budapest.util.GeneralUtil;

public class BinaryDataRecord extends DataRecord
{
   protected final byte[] bytes;
   protected final int len;

   public BinaryDataRecord(JournalFile physicalFile, Bookmark position, byte[] bytes, int len)
   {
      super(physicalFile, position);
      this.bytes = bytes;
      this.len = len;
   }

   @Override
   public String getContent()
   {
      return GeneralUtil.bytesToHexString(bytes, len);
   }

}
