package org.apache.activemq.artemis.budapest.data.record.paging;

import org.apache.activemq.artemis.budapest.data.record.BinaryDataRecord;
import org.apache.activemq.artemis.budapest.iterator.Bookmark;
import org.apache.activemq.artemis.budapest.iterator.JournalFile;
import org.apache.activemq.artemis.core.paging.impl.PagedMessageImpl;

public class PageDataRecord extends BinaryDataRecord
{
   private PagedMessageImpl msg;

   public PageDataRecord(PagedMessageImpl msg, JournalFile curFile, Bookmark position, byte[] rawdata, int len)
   {
      super(curFile, position, rawdata, len);
      this.msg = msg;
   }

   @Override
   public String getContent()
   {
      return msg.toString();
   }

}
