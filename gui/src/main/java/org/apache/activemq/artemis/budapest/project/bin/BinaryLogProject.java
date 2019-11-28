package org.apache.activemq.artemis.budapest.project.bin;

import java.io.File;
import java.io.IOException;

import org.apache.activemq.artemis.budapest.config.ConfigHelper;
import org.apache.activemq.artemis.budapest.data.record.BinaryDataRecord;
import org.apache.activemq.artemis.budapest.data.record.DataRecord;
import org.apache.activemq.artemis.budapest.data.store.BinaryDataStore;
import org.apache.activemq.artemis.budapest.iterator.JournalIterator;
import org.apache.activemq.artemis.budapest.project.AbstractProject;
import org.apache.activemq.artemis.budapest.ui.workspace.DataWorkSpace;
import org.apache.activemq.artemis.budapest.util.GeneralUtil;
import org.w3c.dom.Element;

public class BinaryLogProject extends AbstractProject
{
   private File dataFile;

   public BinaryLogProject(AbstractProject parent, String projectName, DataWorkSpace dataWorkSpace, String projID,
                           File file)
   {
      super(parent, projectName, dataWorkSpace, projID == null ? GeneralUtil.getTimeID() : projID);
      this.dataFile = file;
   }

   public BinaryLogProject(AbstractProject parent, DataWorkSpace dataWorkSpace, File file)
   {
      this(parent, file.getName(), dataWorkSpace, null, file);
   }

   public File getFile()
   {
      return dataFile;
   }

   @Override
   public JournalIterator<? extends DataRecord> iterator() throws IOException
   {
      if (!opened) throw new IllegalStateException("Project not opened yet.");
      BinaryDataStore logStore = new BinaryDataStore(this.dataFile);
      return new JournalIterator<BinaryDataRecord>(logStore, 2048);
   }

   @Override
   public String getType()
   {
      return DataWorkSpace.TYPE_BINARY_LOG;
   }

   @Override
   public void writeAttrs(Element projectElem)
   {
      super.writeAttrs(projectElem);
      projectElem.setAttribute(ConfigHelper.KEY_FILE, this.dataFile.getAbsolutePath());
   }

}
