package org.apache.activemq.artemis.budapest.project.internal;

import org.apache.activemq.artemis.budapest.data.record.journal.JournalDataRecord;
import org.apache.activemq.artemis.budapest.data.store.JournalDataStore;
import org.apache.activemq.artemis.budapest.iterator.JournalIterator;
import org.apache.activemq.artemis.budapest.ui.workspace.DataWorkSpace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.io.File;
import java.io.IOException;

public class JournalProject extends BaseDataProject
{
   private static final Logger logger = LoggerFactory.getLogger(JournalProject.class);

   private DefaultTreeModel dataTreeModel;

   public JournalProject(ArtemisDataProject parent, DataWorkSpace workspace, File dataDir)
   {
      super(parent, "Journal", workspace, dataDir);
      this.dataTreeModel = new DefaultTreeModel(new DefaultMutableTreeNode(this));
   }

   public JournalIterator<JournalDataRecord> iterator() throws IOException
   {
      if (!opened) throw new IllegalStateException("Project not opened yet.");
      JournalDataStore journalStore = new JournalDataStore(dataDir);
      return new JournalIterator<JournalDataRecord>(journalStore, 2048);
   }

   public static DefaultTreeModel emptyLogTreeModel()
   {
      return new DefaultTreeModel(new DefaultMutableTreeNode("log root"));
   }

   public DefaultTreeModel getLogListModel()
   {
      DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode)dataTreeModel.getRoot();
      rootNode.removeAllChildren();

      return dataTreeModel;
   }

   public String stripLineNo(String rec)
   {
      String result = null;
      int index = rec.indexOf("]");
      result = rec.substring(index+1);
      return result;
   }

   public String getDisplayName()
   {
      return this.projectName;
   }

   public void setDisplayName(String newName)
   {
   }

   @Override
   public String getType()
   {
      return DataWorkSpace.TYPE_JOURNAL;
   }

}

