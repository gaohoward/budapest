package org.apache.activemq.artemis.budapest.ui.instance;

import org.apache.activemq.artemis.budapest.data.record.DataRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeywordDecorator implements LogDecorator
{
   private static final Logger logger = LoggerFactory.getLogger(KeywordDecorator.class);
   private String keyword;
   private long current = -1;

   public void setKeyword(String key, long index)
   {
      this.keyword = key;
      this.current = index;
   }

   public void setKeyword(String key)
   {
      this.setKeyword(key, -1);
   }

   public void decorate(RichLogEntryRenderer render, String lineNumber, DataRecord record, int index) throws Exception
   {
      render.decorateWithKeyword(keyword, lineNumber, record, (current == -1), current == index);
   }
}
