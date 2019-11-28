package org.apache.activemq.artemis.budapest.project.internal;

import org.apache.activemq.artemis.budapest.data.record.DataRecord;
import org.apache.activemq.artemis.budapest.data.record.FileNameRecord;
import org.apache.activemq.artemis.budapest.data.store.LargeMessageFileNameStore;
import org.apache.activemq.artemis.budapest.iterator.JournalIterator;
import org.apache.activemq.artemis.budapest.project.AbstractProject;
import org.apache.activemq.artemis.budapest.project.bin.BinaryLogProject;
import org.apache.activemq.artemis.budapest.ui.instance.UserEvent;
import org.apache.activemq.artemis.budapest.ui.workspace.DataWorkSpace;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LargeMessageProject extends BaseDataProject
{
   private Set<String> subProjectSet = new HashSet<>();

   public LargeMessageProject(ArtemisDataProject parent, DataWorkSpace workspace, File file)
   {
      super(parent, "Large Messages", workspace, file, true);
   }

   @Override
   public void deleteChild(AbstractProject child) throws Exception
   {
      super.deleteChild(child);
      BinaryLogProject proj = (BinaryLogProject) child;
      subProjectSet.remove(proj.getFile().getName());
   }

   @Override
   public void addChild(AbstractProject child)
   {
      super.addChild(child);
      subProjectSet.add(((BinaryLogProject)child).getFile().getName());
   }

   @Override
   public String getType()
   {
      return DataWorkSpace.TYPE_LARGEMESSAGES;
   }

   @Override
   public void doAction(String action, Object data) throws Exception
   {
      if (FileNameRecord.OP_SHOW_CONTENTS.equals(action))
      {
         List<DataRecord> list = (List<DataRecord>) data;
         for (DataRecord dr : list)
         {
            FileNameRecord fnr = (FileNameRecord) dr;
            createBinaryProject(fnr);
         }
      }
   }

   public void createBinaryProject(FileNameRecord record)
   {
      String fname = record.getFile().getName();
      if (subProjectSet.add(fname))
      {
         BinaryLogProject project = this.workspace.createBinaryLogProject(this, record.getFile());
         this.workspace.getInstance().broadcast(new UserEvent(this, UserEvent.ASK_OPEN_PROJ, project));
      }
      else
      {
         this.workspace.getInstance().broadcast(new UserEvent(this, UserEvent.TO_PROJECT, this.findChild(fname)));
      }
   }

   private AbstractProject findChild(String fname)
   {
      Map<String, AbstractProject> map = this.getChildren();
      Iterator<AbstractProject> iter = map.values().iterator();
      while (iter.hasNext())
      {
         BinaryLogProject proj = (BinaryLogProject) iter.next();
         if (proj.getFile().getName().equals(fname))
         {
            return proj;
         }
      }
      return null;
   }

   /*
    * At top level we give a list of lm file names.
    * because the number
    * of lm in data dir could be many and we don't want to show them under a tree.
    */
   @Override
   public JournalIterator<FileNameRecord> iterator() throws IOException
   {
      if (!opened) throw new IllegalStateException("Project not opened yet.");
      LargeMessageFileNameStore fileNameStore = new LargeMessageFileNameStore(dataDir);
      return new JournalIterator<FileNameRecord>(fileNameStore, 2048);
   }

}

