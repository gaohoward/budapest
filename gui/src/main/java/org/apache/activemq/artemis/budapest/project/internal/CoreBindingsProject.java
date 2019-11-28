package org.apache.activemq.artemis.budapest.project.internal;

import org.apache.activemq.artemis.budapest.data.record.journal.JournalDataRecord;
import org.apache.activemq.artemis.budapest.data.store.CoreBindingDataStore;
import org.apache.activemq.artemis.budapest.iterator.JournalIterator;
import org.apache.activemq.artemis.budapest.ui.workspace.DataWorkSpace;

import java.io.File;
import java.io.IOException;

public class CoreBindingsProject extends JournalProject
{

   public CoreBindingsProject(BaseDataProject parent, DataWorkSpace workspace, File dataDir)
   {
      this(parent, workspace, null, dataDir);
   }

   public CoreBindingsProject(BaseDataProject parent, DataWorkSpace workspace, String projID, File dataDir)
   {
      super(parent, workspace, dataDir, "Core", projID);
   }

   public JournalIterator<JournalDataRecord> iterator() throws IOException
   {
      if (!opened) throw new IllegalStateException("Project not opened yet.");
      CoreBindingDataStore journalStore = new CoreBindingDataStore(dataDir);
      return new JournalIterator<JournalDataRecord>(journalStore, 2048);
   }

   @Override
   public String getType()
   {
      return DataWorkSpace.TYPE_BINDINGS_CORE;
   }

}
