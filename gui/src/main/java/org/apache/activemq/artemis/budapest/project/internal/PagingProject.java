package org.apache.activemq.artemis.budapest.project.internal;

import org.apache.activemq.artemis.budapest.data.record.DataRecord;
import org.apache.activemq.artemis.budapest.data.record.DirNameRecord;
import org.apache.activemq.artemis.budapest.data.record.FileNameRecord;
import org.apache.activemq.artemis.budapest.data.store.PageDirNameStore;
import org.apache.activemq.artemis.budapest.iterator.JournalIterator;
import org.apache.activemq.artemis.budapest.project.AbstractProject;
import org.apache.activemq.artemis.budapest.ui.instance.UserEvent;
import org.apache.activemq.artemis.budapest.ui.workspace.DataWorkSpace;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PagingProject extends BaseDataProject
{
   //addresses
   private Set<String> subProjectSet = new HashSet<>();

   public PagingProject(ArtemisDataProject parent, DataWorkSpace workspace, File file)
   {
      super(parent, "Paging", workspace, file, true);
   }

   @Override
   public void deleteChild(AbstractProject child) throws Exception
   {
      super.deleteChild(child);
      PageLogProject proj = (PageLogProject) child;
      subProjectSet.remove(proj.getAddress());
   }

   @Override
   public void addChild(AbstractProject child)
   {
      super.addChild(child);
      subProjectSet.add(((PageLogProject)child).getAddress());
   }

   @Override
   public String getType()
   {
      return DataWorkSpace.TYPE_PAGING;
   }

   @Override
   public void doAction(String action, Object data) throws Exception {
      if (FileNameRecord.OP_SHOW_CONTENTS.equals(action))
      {
         List<DataRecord> list = (List<DataRecord>) data;
         for (DataRecord dr : list)
         {
            DirNameRecord fnr = (DirNameRecord) dr;
            createBinaryProject(fnr);
         }
      }
   }
   public void createBinaryProject(DirNameRecord record)
   {
      String address = record.getAddress();
      if (subProjectSet.add(address))
      {
         PageLogProject project = this.workspace.createPageLogProject(this, record.getFile());
         this.workspace.getInstance().broadcast(new UserEvent(this, UserEvent.ASK_OPEN_PROJ, project));
      }
      else
      {
         this.workspace.getInstance().broadcast(new UserEvent(this, UserEvent.TO_PROJECT, this.findChild(address)));
      }
   }

   private AbstractProject findChild(String address) {
      Map<String, AbstractProject> map = this.getChildren();
      Iterator<AbstractProject> iter = map.values().iterator();
      while (iter.hasNext()) {
         PageLogProject proj = (PageLogProject) iter.next();
         if (proj.getAddress().equals(address)) {
            return proj;
         }
      }
      return null;
   }

   /*
    * At top level we give a list of page dirs
    * under data/paging. A page dir has a uuid str
    * as its name. Inside there is a address.txt
    * file which we can extract the address.
    */
   @Override
   public JournalIterator<DirNameRecord> iterator() throws IOException
   {
      if (!opened) throw new IllegalStateException("Project not opened yet.");
      PageDirNameStore dirNameStore = new PageDirNameStore(dataDir);
      return new JournalIterator<DirNameRecord>(dirNameStore, 2048);
   }

}


