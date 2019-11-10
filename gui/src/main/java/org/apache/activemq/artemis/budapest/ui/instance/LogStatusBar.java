package org.apache.activemq.artemis.budapest.ui.instance;

import org.apache.activemq.artemis.budapest.project.book.StatusInfo;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class LogStatusBar
{
   private LinkedHashMap<String, JLabel> fields;

   public LogStatusBar(MainInstance instance, JComponent container)
   {
      fields = new LinkedHashMap<String, JLabel>();

      JPanel panel = new JPanel();
      panel.setLayout(new BorderLayout());

      JLabel generalStatus = new JLabel("");
      fields.put(StatusInfo.GENERAL, generalStatus);

      JPanel logPanel = new JPanel();
      logPanel.setLayout(new FlowLayout());

      JLabel pageTitle = new JLabel(StatusInfo.PAGENO);
      JLabel pageLabel = new JLabel("");
      fields.put(StatusInfo.PAGENO, pageLabel);

      JLabel lineTitle = new JLabel(StatusInfo.LINENO);
      JLabel lineLabel = new JLabel("");
      fields.put(StatusInfo.LINENO, lineLabel);

      logPanel.add(pageTitle);
      logPanel.add(pageLabel);
      logPanel.add(lineTitle);
      logPanel.add(lineLabel);

      panel.add(generalStatus, BorderLayout.CENTER);
      panel.add(logPanel, BorderLayout.EAST);

      container.add(panel, BorderLayout.SOUTH);
   }

   public void showStatus(String text)
   {
      showStatus(new StatusInfo(text));
   }

   public void showStatus(StatusInfo info)
   {
      HashMap<String, Object> statusMap = info.getStatusMap();
      Iterator<Map.Entry<String, Object>> iter = statusMap.entrySet().iterator();
      while (iter.hasNext())
      {
         Map.Entry<String, Object> entry = iter.next();
         String key = entry.getKey();
         Object value = entry.getValue();
         JLabel label = fields.get(key);
         if (label != null)
         {
            label.setText(value.toString());
         }
      }
   }
}
