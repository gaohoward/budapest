package org.apache.activemq.artemis.budapest.project.internal;

import org.apache.activemq.artemis.budapest.config.ConfigHelper;
import org.apache.activemq.artemis.budapest.data.record.DataRecord;
import org.apache.activemq.artemis.budapest.data.record.paging.PageDataRecord;
import org.apache.activemq.artemis.budapest.data.store.PagingDataStore;
import org.apache.activemq.artemis.budapest.iterator.JournalIterator;
import org.apache.activemq.artemis.budapest.project.AbstractProject;
import org.apache.activemq.artemis.budapest.ui.workspace.DataWorkSpace;
import org.apache.activemq.artemis.budapest.util.GeneralUtil;
import org.w3c.dom.Element;

import java.io.File;
import java.io.IOException;

public class PageLogProject extends AbstractProject
{
   private File addressDir;

   public PageLogProject(AbstractProject parent, String projectName, DataWorkSpace dataWorkSpace, String projID,
                         File dir)
   {
      super(parent, projectName, dataWorkSpace, projID == null ? GeneralUtil.getTimeID() : projID);
      this.addressDir = dir;
   }

   public PageLogProject(AbstractProject parent, DataWorkSpace dataWorkSpace, File dir)
   {
      this(parent, dir.getName(), dataWorkSpace, null, dir);
   }

   public File getFile()
   {
      return addressDir;
   }

   @Override
   public JournalIterator<? extends DataRecord> iterator() throws IOException
   {
      if (!opened) throw new IllegalStateException("Project not opened yet.");
      PagingDataStore logStore = new PagingDataStore(this.addressDir);
      return new JournalIterator<PageDataRecord>(logStore, 2048);
   }

   @Override
   public String getType()
   {
      return DataWorkSpace.TYPE_PAGE_LOG;
   }

   @Override
   public void writeAttrs(Element projectElem)
   {
      super.writeAttrs(projectElem);
      projectElem.setAttribute(ConfigHelper.KEY_BASEDIR, this.addressDir.getAbsolutePath());
   }

   public String getAddress()
   {
      return GeneralUtil.parseAddress(addressDir);
   }

}
