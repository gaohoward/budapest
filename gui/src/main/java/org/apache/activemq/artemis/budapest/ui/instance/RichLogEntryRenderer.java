package org.apache.activemq.artemis.budapest.ui.instance;

import org.apache.activemq.artemis.budapest.data.record.DataRecord;
import org.apache.activemq.artemis.budapest.ui.LogExplorer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JList;
import javax.swing.JTextPane;
import javax.swing.ListCellRenderer;
import javax.swing.text.Highlighter;
import javax.swing.text.StyledDocument;
import java.awt.Color;
import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RichLogEntryRenderer extends JTextPane implements ListCellRenderer<DataRecord>
{
   private static final long serialVersionUID = -9061114361582361824L;

   private static final Logger logger = LoggerFactory.getLogger(RichLogEntryRenderer.class);

   private DataProjectModel model;

   private StyledDocument document;
   private LogExplorer explorer;
   private RichTextRenderer renderer;
   private List<Highlight> highlightList = new ArrayList<Highlight>();

   public RichLogEntryRenderer(LogExplorer explorer, int fontSize, DataProjectModel model)
   {
      this.model = model;
      this.explorer = explorer;
      //set font and size styles
      document = getStyledDocument();
      renderer = new RichTextRenderer(document, fontSize);
   }

   private String format(final long lineNumber)
   {
      if (lineNumber > Long.MAX_VALUE)
      {
         throw new IllegalArgumentException("Line number out of scope! " + lineNumber);
      }
      long pageNum = model.getCurrentPageNumber();
      int pageSize = model.getPageSize();
      int currSize = model.getSize();
      long maxInPageIndex = pageNum * pageSize;
      if (pageSize > currSize)
      {
         maxInPageIndex = maxInPageIndex - pageSize + currSize;
      }

      String maxStr = "" + maxInPageIndex;
      int len = maxStr.length();
      return String.format("%1$0" + len + "d ", lineNumber);
   }

   public void defaultDecorate(String ln, DataRecord record) throws Exception
   {
      document.insertString(document.getLength(), ln, renderer.getStyle(RichTextRenderer.LINE_NUMBER));
      document.insertString(document.getLength(), record.getDisplayString(), renderer.getStyle(RichTextRenderer.NORM_FONT_STYLE));
   }

   public void decorateWithKeyword(String keyword, String ln, DataRecord record, boolean always, boolean isCurrent) throws Exception
   {
      String log = record.getContent();
      String logNoCase = log.toLowerCase();
      highlightList.clear();
      boolean keyPresented = keyword != null && !keyword.isEmpty();
      if (keyPresented)
      {
         highlightList.add(new Highlight(keyword, RichTextRenderer.CURRENT_KEYWORD));
      }

      if (keyPresented && !always && logNoCase.contains(keyword.toLowerCase()))
      {
         document.insertString(document.getLength(), ln, renderer.getStyle(RichTextRenderer.HIGH_LINE_NUMBER));
      }
      else
      {
         document.insertString(document.getLength(), ln, renderer.getStyle(RichTextRenderer.LINE_NUMBER));
      }
      renderer.formatText(record.getDisplayString(), highlightList);
   }

   public Component getListCellRendererComponent(JList<? extends DataRecord> list,
                                                 DataRecord value,
                                                 int index,
                                                 boolean isSelected,
                                                 boolean cellHasFocus)
   {
      try
      {
         long pn = model.getLineNumber(index);
         String line = format(pn);
         document.remove(0, document.getLength());
         explorer.getDecorator().decorate(this, line, value, index);
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }

      Color background;
      Color foreground;

      if (isSelected)
      {
         background = Color.LIGHT_GRAY;
         foreground = Color.WHITE;

         // unselected, and not the DnD drop location
      }
      else
      {
         background = Color.WHITE;
         foreground = Color.BLACK;
      }

      setBackground(background);
      setForeground(foreground);
      File srcFile = value.getSourceFile().getFile();

      this.setToolTipText(srcFile == null ? "" : srcFile.getAbsolutePath());

      return this;
   }

}
