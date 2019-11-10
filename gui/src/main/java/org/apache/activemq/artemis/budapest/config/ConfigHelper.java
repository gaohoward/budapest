package org.apache.activemq.artemis.budapest.config;

import org.apache.activemq.artemis.budapest.project.AbstractProject;
import org.apache.activemq.artemis.budapest.ui.workspace.DataWorkSpace;
import org.apache.activemq.artemis.budapest.ui.workspace.WorkspaceEvent;
import org.apache.activemq.artemis.budapest.ui.workspace.WorkspaceEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 <budapest-projects>
 <project base-dir="/home/howard/tests/apache-artemis-2.11.0-SNAPSHOT/bin/broker0" id="1573292314335" is-artemis-instance="true" name="broker0" parent-id="" type="1">
 <children>
 <child id="1573292314336"/>
 <child id="1573292314337"/>
 <child id="1573292314338"/>
 <child id="1573292314339"/>
 </children>
 </project>
 <project base-dir="/home/howard/tests/apache-artemis-2.11.0-SNAPSHOT/bin/broker0/data/journal" id="1573292314336" name="Journal" parent-id="1573292314335" type="2">
 <children>
 <child id="1573293272437"/>
 </children>
 </project>
 <project base-dir="/home/howard/tests/apache-artemis-2.11.0-SNAPSHOT/bin/broker0/data/bindings" id="1573292314337" name="Bindings" parent-id="1573292314335" type="3">
 <children/>
 </project>
 <project base-dir="/home/howard/tests/apache-artemis-2.11.0-SNAPSHOT/bin/broker0/data/paging" id="1573292314338" name="Paging" parent-id="1573292314335" type="4">
 <children/>
 </project>
 <project base-dir="/home/howard/tests/apache-artemis-2.11.0-SNAPSHOT/bin/broker0/data/large-messages" id="1573292314339" name="Large Messages" parent-id="1573292314335" type="5">
 <children/>
 </project>
 <project filter-type="0" filter-value="390\ bytes" id="1573293272437" name="390\ bytes" parent-id="1573292314336" type="6">
 <children/>
 </project>

 </budapest-projects>

 */
public class ConfigHelper implements WorkspaceEventListener
{
   private static final Logger logger = LoggerFactory.getLogger(ConfigHelper.class);
   public static final String KEY_PROJECTS = "budapest-projects";
   public static final String KEY_PROJECT = "project";
   public static final String KEY_CHILDREN = "children";
   public static final String KEY_CHILD = "child";
   public static final String KEY_PARENT = "parent-id";
   public static final String KEY_ID = "id";
   public static final String KEY_NAME = "name";
   public static final String KEY_TYPE = "type";
   public static final String KEY_BASEDIR = "base-dir";
   public static final String KEY_ISARTEMISINSTANCE = "is-artemis-instance";
   public static final String KEY_FILTER_TYPE = "filter-type";
   public static final String KEY_FILTER_VALUE = "filter-value";

   private MainConfig config;
   private Element rootElem;
   private Map<String, Element> projectMap = new HashMap<>();

   public ConfigHelper(MainConfig config) throws Exception
   {
      this.config = config;
      init();
   }

   public void init() throws Exception
   {
      rootElem = config.getRoot();
      NodeList nlist = rootElem.getElementsByTagName(KEY_PROJECT);
      for (int i = 0; i < nlist.getLength(); i++)
      {
         Element projElem = (Element) nlist.item(i);
         String projId = projElem.getAttribute(KEY_ID);
         projectMap.put(projId, projElem);
      }
      config.writeConfig();
   }

   public void removeProject(AbstractProject proj) throws Exception
   {
      internalRemove(proj);
      config.writeConfig();
   }

   private void internalRemove(AbstractProject proj) throws Exception
   {
      logger.debug("removing {} from config...", proj.getProjectName());

      AbstractProject parent = proj.getParent();

      if (parent != null)
      {
         String pid = parent.getID();
         Element parentElem = projectMap.get(pid);
         //remove child
         Element childrenElem = (Element) parentElem.getElementsByTagName(KEY_CHILDREN).item(0);
         NodeList childList = childrenElem.getElementsByTagName(KEY_CHILD);
         for (int i = 0; i < childList.getLength(); i++)
         {
            Element child = (Element) childList.item(i);
            if (proj.getID().equals(child.getAttribute(KEY_ID)))
            {
               childrenElem.removeChild(child);
               break;
            }
         }
      }

      //remove children
      Map<String, AbstractProject> children = proj.getChildren();
      for (AbstractProject child : children.values())
      {
         internalRemove(child);
      }

      //remove self
      Element projElem = projectMap.remove(proj.getID());
      rootElem.removeChild(projElem);
   }

   public List<AbstractProject> loadProjects(DataWorkSpace workspace) throws IOException
   {
      List<AbstractProject> topProjects = new ArrayList<AbstractProject>();
      Iterator<String> iterProjs = projectMap.keySet().iterator();

      Map<String, AbstractProject> missingChildren = new HashMap<String, AbstractProject>();
      Map<String, List<AbstractProject>> missingParents = new HashMap<String, List<AbstractProject>>();
      Map<String, AbstractProject> loadedProjects = new HashMap<String, AbstractProject>();

      while (iterProjs.hasNext())
      {
         String id = iterProjs.next();
         Element projectElement = projectMap.get(id);
         String projID = projectElement.getAttribute(KEY_ID);
         String parentID = projectElement.getAttribute(KEY_PARENT);
         String projectType = projectElement.getAttribute(KEY_TYPE);
         String projectName = projectElement.getAttribute(KEY_NAME);
         logger.debug("loading project {}, id={}, parentid={}, type={}", projID, parentID, projectType);

         AbstractProject project = workspace.loadProject(projectType, projID, projectName, projectElement);

         //who is looking for me as its child?
         AbstractProject cfinder = missingChildren.get(projID);
         if (cfinder != null)
         {
            logger.debug("I am a child of project: {}", cfinder.getID());
            cfinder.addChild(project);
         }
         //who are looking for me as their parent
         List<AbstractProject> pfinders = missingParents.get(projID);
         if (pfinders != null)
         {
            for (AbstractProject child : pfinders)
            {
               logger.debug("I am the parent of project: {}", child.getID());
               child.setParent(project);
            }
         }

         if (parentID != null && !parentID.trim().equals(""))
         {
            logger.debug("Looking for my parent id={}", parentID);
            //where is my parent
            AbstractProject myParent = loadedProjects.get(parentID);
            if (myParent == null)
            {
               logger.debug("can't find my parent for now, regitster...");
               //missing parent
               List<AbstractProject> cList = missingParents.get(parentID);
               if (cList == null)
               {
                  cList = new ArrayList<AbstractProject>();
                  missingParents.put(parentID, cList);
               }
               cList.add(project);
            }
            else
            {
               logger.debug("found my parent {}", myParent.getID());
               project.setParent(myParent);
            }
         }
         else
         {
            logger.debug("I'm a top project no parent.");
            //top project
            topProjects.add(project);
         }

         //where are my children
         NodeList childList = projectElement.getElementsByTagName(KEY_CHILD);
         for (int i = 0; i < childList.getLength(); i++)
         {
            Element childItem = (Element)childList.item(i);
            String childID = childItem.getAttribute(KEY_ID);

            logger.debug("I have a child: {}, looking it up...", childID);
            AbstractProject myChild = loadedProjects.get(childID);
            if (myChild == null)
            {
               logger.debug("my child {} is not found yet, register...", childID);
               missingChildren.put(childID, project);
            }
            else
            {
               logger.debug("found my child {}, add it", myChild.getID());
               project.addChild(myChild);
            }
         }
         //put to pool
         logger.debug("put my self into pool {}", projID);
         loadedProjects.put(projID, project);
      }

      return topProjects;
   }

   public void addProject(AbstractProject proj) throws Exception
   {
      Element projectElem = rootElem.getOwnerDocument().createElement(KEY_PROJECT);
      rootElem.appendChild(projectElem);

      proj.writeAttrs(projectElem);

      AbstractProject parent = proj.getParent();

      //children (must be empty for a new proj)
      Element childrenElem = rootElem.getOwnerDocument().createElement(KEY_CHILDREN);
      projectElem.appendChild(childrenElem);

      if (parent != null)
      {
         String pid = parent.getID();
         Element parentElem = projectMap.get(pid);
         NodeList nlist = parentElem.getElementsByTagName(KEY_CHILDREN);
         childrenElem = (Element) nlist.item(0);
         Element child = rootElem.getOwnerDocument().createElement(KEY_CHILD);
         child.setAttribute(KEY_ID, proj.getID());
         childrenElem.appendChild(child);
      }

      config.writeConfig();
      projectMap.put(proj.getID(), projectElem);
   }

   public void handleEvent(WorkspaceEvent event)
   {
      try
      {
         AbstractProject proj = (AbstractProject)event.getEventData();
         switch (event.getEventType())
         {
            case WorkspaceEvent.NEW_PROJECT:
               addProject(proj);
               break;
            case WorkspaceEvent.PROJECT_REMOVED:
               removeProject(proj);
               break;
            default:
               break;
         }
      }
      catch (Exception e)
      {
         logger.error("Failed to handle event", e);
      }
   }

   @Override
   public String toString()
   {
      return config.toString();
   }

   public File getInstanceDataDir(String name) {
      return config.getInstanceDir(name);
   }
}
