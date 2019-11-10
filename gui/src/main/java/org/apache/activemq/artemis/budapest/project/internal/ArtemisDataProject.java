package org.apache.activemq.artemis.budapest.project.internal;

import org.apache.activemq.artemis.budapest.config.ConfigHelper;
import org.apache.activemq.artemis.budapest.data.record.DataRecord;
import org.apache.activemq.artemis.budapest.iterator.JournalIterator;
import org.apache.activemq.artemis.budapest.ui.workspace.DataWorkSpace;
import org.apache.activemq.artemis.budapest.util.GeneralUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.io.File;
import java.io.IOException;

public class ArtemisDataProject extends BaseDataProject
{
   private static final Logger logger = LoggerFactory.getLogger(ArtemisDataProject.class);
   private boolean isArtemisInstance = true;

   private DefaultTreeModel dataTreeModel;

   public ArtemisDataProject(String id, String prjName, DataWorkSpace workspace, File baseDir, boolean isArtemisInstance)
   {
      super(null, prjName, workspace, baseDir, id);
      this.isArtemisInstance = isArtemisInstance;

      this.dataTreeModel = new DefaultTreeModel(new DefaultMutableTreeNode(this));
   }

   public ArtemisDataProject(String prjName, DataWorkSpace workspace, File baseDir, boolean isArtemisInstance)
   {
      this(GeneralUtil.getTimeID(), prjName, workspace, baseDir, isArtemisInstance);
   }

   public static DefaultTreeModel emptyLogTreeModel()
   {
      return new DefaultTreeModel(new DefaultMutableTreeNode("project root"));
   }

   public boolean supportIteration()
   {
      return false;
   }

   public DefaultTreeModel getLogListModel()
   {
      return dataTreeModel;
   }

   public boolean isArtemisInstance()
   {
      return this.isArtemisInstance;
   }

   public void init()
   {
      if (isArtemisInstance)
      {
         workspace.createJournalProject(this, new File(dataDir, "data/journal"));
         workspace.createBindingsProject(this, new File(dataDir, "data/bindings"));
         workspace.createPagingProject(this, new File(dataDir, "data/paging"));
         workspace.createLargeMessageProject(this, new File(dataDir, "data/large-messages"));
      }
      else
      {
         workspace.createJournalProject(this, new File(dataDir, "journal"));
         workspace.createBindingsProject(this, new File(dataDir, "bindings"));
         workspace.createPagingProject(this, new File(dataDir, "paging"));
         workspace.createLargeMessageProject(this, new File(dataDir, "large-messages"));
      }
   }

   public void writeAttrs(Element projectElem)
   {
      super.writeAttrs(projectElem);
      projectElem.setAttribute(ConfigHelper.KEY_ISARTEMISINSTANCE, String.valueOf(isArtemisInstance));
   }

   @Override
   public String getType()
   {
      return DataWorkSpace.TYPE_ARTEMIS;
   }

   @Override
   public JournalIterator<? extends DataRecord> iterator() throws IOException
   {
      return null;
   }

}
