package org.apache.activemq.artemis.budapest.project.internal;

import org.apache.activemq.artemis.budapest.data.record.DataRecord;
import org.apache.activemq.artemis.budapest.iterator.JournalIterator;
import org.apache.activemq.artemis.budapest.ui.workspace.DataWorkSpace;

import java.io.File;
import java.io.IOException;

public class BindingsProject extends BaseDataProject
{

   public BindingsProject(ArtemisDataProject parent, DataWorkSpace workspace, File file)
   {
      super(parent, "Bindings", workspace, file);
   }

   @Override
   public String getType()
   {
      return DataWorkSpace.TYPE_BINDINGS;
   }

   @Override
   public JournalIterator<? extends DataRecord> iterator() throws IOException
   {
      // TODO Auto-generated method stub
      return null;
   }

}
