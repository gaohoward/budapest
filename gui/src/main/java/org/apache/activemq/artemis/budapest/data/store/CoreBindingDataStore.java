package org.apache.activemq.artemis.budapest.data.store;

import java.io.File;
import java.io.IOException;

public class CoreBindingDataStore extends JournalDataStore
{

   public CoreBindingDataStore(File journalDir) throws IOException {
      super(journalDir);
   }

   @Override
   //for core binding journals the prefix is ‘activemq-bindings’ and the extension(sufix) is ‘bindings’
   public boolean isJournalFile(File dir, String name) {
      return name.endsWith(".bindings");
   }

}
