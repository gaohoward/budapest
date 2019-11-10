package org.apache.activemq.artemis.budapest.ui.instance;

import org.apache.activemq.artemis.budapest.data.record.DataRecord;

public interface LogDecorator
{
   void decorate(RichLogEntryRenderer render, String lineNumber, DataRecord record, int index) throws Exception;
}
