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
   protected boolean readOnly;

   private LinkedHashMap<String, AbstractProject> children = new LinkedHashMap<>();

   //for convenience of checking
   private Set<FilterString> filteredSet = new HashSet<>();

   protected DefaultTreeModel logTreeModel;

   public AbstractProject(AbstractProject parent, String projectName, DataWorkSpace workspace, String projId)
  {
    this(parent,  projectName, workspace, projId, false);
  }

  public AbstractProject(AbstractProject parent, String projectName, DataWorkSpace workspace, String projId, boolean readOnly)
  {
    this.parent = parent;
    if (this.parent != null)
    {
      //directly access children rather than call addChild()
      //because it'll be troublesome if addChild() is overriden
      //not so good doing it in constructor (if addChild() is
      //overriden in this class and it's called from parent,
      //and if it accesses this class' member field, the field
      //may not be initiated thus null.
      this.parent.children.put(this.getID(), this);
    }
    this.projectName = projectName;
    this.workspace = workspace;
    this.id = projId == null ? GeneralUtil.getTimeID() : projId;
    this.logTreeModel = new DefaultTreeModel(new DefaultMutableTreeNode(this));
    this.readOnly = readOnly;
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

   public void delete() throws Exception
   {
      if (parent == null)
      {
         workspace.deleteProject(this);
      }
      else
      {
         logger.debug("I've parent, let it do delete");
         parent.deleteChild(this);
      }
   }

   public void deleteChild(AbstractProject child) throws Exception
   {
      logger.debug("deleting my child: {}", child.getID());
      AbstractProject toRemove = children.remove(child.getID());

      if (toRemove != null)
      {
         toRemove.doDelete();
      }
      else
      {
         logger.warn("no such child!, {}", children);
      }
   }

   public JournalIterator<? extends DataRecord> iterator() throws IOException
   {
      return null;
   }

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

   //not this method can be overriden
   //as long as it's not called from constructor.
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

   public void doDelete() throws Exception
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

   public void doAction(String action, Object data) throws Exception
   {
      throw new IllegalStateException("NO action defined for " + this);
   }

   public boolean isReadOnly()
   {
      return readOnly;
   }

}
