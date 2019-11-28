package org.apache.activemq.artemis.budapest.data.store;

import java.io.File;
import java.io.IOException;

public class JmsBindingDataStore extends JournalDataStore
{

   public JmsBindingDataStore(File journalDir) throws IOException {
      super(journalDir);
   }

   @Override
   //for jms binding journals the prefix is ‘activemq-jms’ and the extension(sufix) is ‘jms’
   public boolean isJournalFile(File dir, String name) {
      return name.endsWith(".jms");
   }

}
