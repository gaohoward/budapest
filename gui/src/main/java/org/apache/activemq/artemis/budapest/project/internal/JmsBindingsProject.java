package org.apache.activemq.artemis.budapest.project.internal;

import org.apache.activemq.artemis.budapest.data.record.journal.JournalDataRecord;
import org.apache.activemq.artemis.budapest.data.store.JmsBindingDataStore;
import org.apache.activemq.artemis.budapest.iterator.JournalIterator;
import org.apache.activemq.artemis.budapest.ui.workspace.DataWorkSpace;

import java.io.File;
import java.io.IOException;

public class JmsBindingsProject extends JournalProject
{
   public JmsBindingsProject(BaseDataProject parent, DataWorkSpace workspace, File dataDir)
   {
      this(parent, workspace, null, dataDir);
   }

   public JmsBindingsProject(BaseDataProject parent, DataWorkSpace workspace, String projID, File dataDir)
   {
      super(parent, workspace, dataDir, "Jms", projID);
   }

   public JournalIterator<JournalDataRecord> iterator() throws IOException
   {
      if (!opened) throw new IllegalStateException("Project not opened yet.");
      JmsBindingDataStore journalStore = new JmsBindingDataStore(dataDir);
      return new JournalIterator<JournalDataRecord>(journalStore, 2048);
   }

   @Override
   public String getType()
   {
      return DataWorkSpace.TYPE_BINDINGS_JMS;
   }
}
