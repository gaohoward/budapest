package org.apache.activemq.artemis.budapest.project;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.apache.activemq.artemis.budapest.config.ConfigHelper;
import org.apache.activemq.artemis.budapest.data.record.DataRecord;
import org.apache.activemq.artemis.budapest.iterator.JournalIterator;
import org.apache.activemq.artemis.budapest.project.plain.PlainLogProject;
import org.apache.activemq.artemis.budapest.ui.LogExplorer;
import org.apache.activemq.artemis.budapest.ui.workspace.DataWorkSpace;
import org.apache.activemq.artemis.budapest.ui.workspace.WorkspaceEvent;
import org.apache.activemq.artemis.budapest.util.GeneralUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

public abstract class AbstractProject
{
   private static final Logger logger = LoggerFactory.getLogger(AbstractProject.class);

   protected AbstractProject parent;
   protected String projectName;
   protected DataWorkSpace workspace;
   protected String id;
   protected boolean opened;

   protected LinkedHashMap<String, AbstractProject> children = new LinkedHashMap<>();

   //for convenience of checking
   private Set<FilterString> filteredSet = new HashSet<>();

   protected DefaultTreeModel logTreeModel;

   public AbstractProject(AbstractProject parent, String projectName, DataWorkSpace workspace)
   {
      this(parent, projectName, workspace, GeneralUtil.getTimeID());
   }

   public AbstractProject(AbstractProject parent, String projectName, DataWorkSpace workspace, String projId)
   {
      this.parent = parent;
      if (this.parent != null)
      {
         this.parent.addChild(this);
      }
      this.projectName = projectName;
      this.workspace = workspace;
      this.id = projId;
      this.logTreeModel = new DefaultTreeModel(new DefaultMutableTreeNode(this));
   }

   public void close()
   {
      if (opened)
      {
         opened = false;
         workspace.eventHappened(new WorkspaceEvent(WorkspaceEvent.PROJECT_CLOSED, this));
      }
   }

   public void open() throws Exception
   {
      if (!opened)
      {
         opened = true;
      }
   }

   public AbstractProject getParent()
   {
      return this.parent;
   }

   public String getID()
   {
      return this.id;
   }

   public String getProjectName()
   {
      return this.projectName;
   }

   public DataWorkSpace getWorkspace()
   {
      return this.workspace;
   }

   public boolean isOpened()
   {
      return opened;
   }

   public String toString()
   {
      return projectName;
   }

   public void delete() throws IOException
   {
      logger.debug("deleteing me: {}", projectName);
      workspace.deleteProject(this);
   }

   public abstract JournalIterator<? extends DataRecord> iterator() throws IOException;

   public AbstractProject createQueryLog(int type, String value, LogExplorer logExplorer)
   {
      FilterString filter = new FilterString(value, type);

      if (filteredSet.add(filter))
      {
         PlainLogProject subProject = this.workspace.createPlainLogProject(this, filter);
         children.put(subProject.getID(), subProject);
         subProject.doFiltering(logExplorer);
         return subProject;
      }
      return null;
   }

   public Map<String, AbstractProject> getChildren()
   {
      return this.children;
   }

   public boolean supportIteration()
   {
      return true;
   }

   public DefaultTreeModel getLogListModel()
   {
      return logTreeModel;
   }

   public void addChild(AbstractProject child)
   {
      children.put(child.getID(), child);
   }

   public void setParent(AbstractProject project)
   {
      this.parent = project;
   }

   public void setProjectName(String newProjName)
   {
      this.projectName = newProjName;
   }

   public void setID(String projID)
   {
      this.id = projID;
   }

   public abstract String getType();

   public void writeAttrs(Element projectElem)
   {
      projectElem.setAttribute(ConfigHelper.KEY_TYPE, getType());
      projectElem.setAttribute(ConfigHelper.KEY_NAME, getProjectName());
      projectElem.setAttribute(ConfigHelper.KEY_ID, getID());
      projectElem.setAttribute(ConfigHelper.KEY_PARENT, parent == null ? "" : parent.getID());
   }

   public void doDelete()
   {
      logger.debug("Now real deleting {}", id);
      close();

      if (this.children.size() > 0)
      {
         Iterator<AbstractProject> iter = children.values().iterator();
         while (iter.hasNext())
         {
            AbstractProject proj = iter.next();
            proj.doDelete();
         }
         children.clear();
      }

      logger.debug("project deleted ", id);
      workspace.eventHappened(new WorkspaceEvent(WorkspaceEvent.PROJECT_REMOVED, this));
   }

}
