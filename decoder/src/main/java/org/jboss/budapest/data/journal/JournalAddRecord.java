package org.jboss.budapest.data.journal;

import org.apache.activemq.artemis.core.journal.impl.JournalImpl;
import org.jboss.budapest.data.repository.DataStoreInputStream;

import java.io.IOException;

public class JournalAddRecord extends JournalAddRecordBase {

   public JournalAddRecord() {
      super(JournalImpl.ADD_RECORD);
   }
}
