package org.apache.activemq.artemis.budapest.ui.instance;

import org.apache.activemq.artemis.budapest.data.store.LogFile;
import org.apache.activemq.artemis.budapest.ui.UIHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.Component;
import java.net.URL;

public class LogListTreeRenderer extends DefaultTreeCellRenderer
{
   private static final Logger logger = LoggerFactory.getLogger(LogListTreeRenderer.class);
   private int currentRow;
   private Object currentValue;
   private Icon singleIcon;
   private Icon compositeIconOpen;
   private Icon compositeIconClosed;

   public LogListTreeRenderer()
   {
      try
      {
         //set normal icons here.
         final String iconLoc = "/icons/project" + UIHelper.getIconSize() + ".png";
         final String iconLoc2 = "/icons/project" + UIHelper.getIconSize() + ".png";
         final String iconLoc3 = "/icons/project" + UIHelper.getIconSize() + ".png";
         URL url = LogProjectTreeRenderer.class.getResource(iconLoc);
         URL url2 = LogProjectTreeRenderer.class.getResource(iconLoc2);
         URL url3 = LogProjectTreeRenderer.class.getResource(iconLoc3);
         singleIcon = new ImageIcon(url);
         compositeIconOpen = new ImageIcon(url2);
         compositeIconClosed = new ImageIcon(url3);
      }
      catch (Exception e)
      {
         logger.warn("Error creating tree icons", e);
      }
   }

   @Override
   public Component getTreeCellRendererComponent(JTree tree,
                                                 Object value,
                                                 boolean selected,
                                                 boolean expanded,
                                                 boolean leaf,
                                                 int row,
                                                 boolean hasFocus)
   {
      currentRow = row;
      DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
      currentValue = node.getUserObject();
      return super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
   }

   @Override
   public Icon getClosedIcon()
   {
      if (currentValue instanceof LogFile)
      {
         return singleIcon;
      }
      return super.getClosedIcon();
   }

   @Override
   public Icon getOpenIcon()
   {
      if (currentValue instanceof LogFile)
      {
         return singleIcon;
      }
      return super.getOpenIcon();
   }

   @Override
   public Icon getLeafIcon()
   {
      if (currentValue instanceof LogFile)
      {
         return singleIcon;
      }
      return super.getLeafIcon();
   }

}
