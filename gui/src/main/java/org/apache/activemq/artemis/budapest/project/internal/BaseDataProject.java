package org.apache.activemq.artemis.budapest.project.internal;

import org.apache.activemq.artemis.budapest.config.ConfigHelper;
import org.apache.activemq.artemis.budapest.project.AbstractProject;
import org.apache.activemq.artemis.budapest.ui.workspace.DataWorkSpace;
import org.apache.activemq.artemis.budapest.util.GeneralUtil;
import org.w3c.dom.Element;

import java.io.File;

public abstract class BaseDataProject extends AbstractProject
{
   protected File dataDir;

   public BaseDataProject(BaseDataProject parent, String projectName, DataWorkSpace workspace, File dataDir)
   {
      this(parent, projectName, workspace, dataDir, GeneralUtil.getTimeID());
   }

   public BaseDataProject(BaseDataProject parent, String projectName, DataWorkSpace workspace, File dataDir, String id)
   {
      super(parent, projectName, workspace, id);
      this.dataDir = dataDir;
   }

   public File getDataDir()
   {
      return this.dataDir;
   }

   public void writeAttrs(Element projectElem)
   {
      super.writeAttrs(projectElem);
      projectElem.setAttribute(ConfigHelper.KEY_BASEDIR, getDataDir().getAbsolutePath());
   }
}
