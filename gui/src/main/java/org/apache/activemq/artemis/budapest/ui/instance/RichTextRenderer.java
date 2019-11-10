package org.apache.activemq.artemis.budapest.ui.instance;

import org.apache.activemq.artemis.budapest.ui.UIHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RichTextRenderer
{
   private static final Logger logger = LoggerFactory.getLogger(RichTextRenderer.class);

   public static final String NORM_FONT_STYLE = "normal";
   public static final String BOLD_FONT_STYLE = "bold";
   public static final String ITALIC_FONT_STYLE = "italic";
   public static final String UNDERLINE = "underline";
   public static final String LINE_NUMBER = "line-number";
   public static final String HIGH_LINE_NUMBER = "high-line-number";
   public static final String KEYWORD = "keyword";
   public static final String CURRENT_KEYWORD = "current-keyword";
   public static final String ERROR_LOG = "error-log";

   private StyledDocument document;
   private int fontSz;
   private Style normal, italic, bold, underline, linenumber, high_linenumber, scurrent_key, skeyword, error_log;
   private Map<String, Style> styles = new HashMap<String, Style>();
   private List<StringSlice> slices = new ArrayList<StringSlice>();

   private static Set<Highlight> globalHighlights = new HashSet<Highlight>();

   private static Set<Highlight> predefinedHighlights = new HashSet<Highlight>();

   public RichTextRenderer(StyledDocument document, int fontSz)
   {
      this.document = document;
      this.fontSz = fontSz;
      initStyles();
   }

   private void initStyles()
   {
      Style basic = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);

      normal = document.addStyle(NORM_FONT_STYLE, basic);
      StyleConstants.setFontFamily(normal, "SansSerif");
      StyleConstants.setFontSize(normal, fontSz);
      StyleConstants.setBold(normal, false);
      styles.put(NORM_FONT_STYLE, normal);

      italic = document.addStyle(ITALIC_FONT_STYLE, normal);
      StyleConstants.setItalic(italic, true);
      styles.put(ITALIC_FONT_STYLE, italic);

      bold = document.addStyle(BOLD_FONT_STYLE, normal);
      StyleConstants.setBold(bold, true);
      styles.put(BOLD_FONT_STYLE, bold);

      underline = document.addStyle(UNDERLINE, normal);
      StyleConstants.setUnderline(underline, true);
      styles.put(UNDERLINE, underline);

      linenumber = document.addStyle(LINE_NUMBER, normal);
      StyleConstants.setBold(linenumber, true);
      StyleConstants.setFontSize(linenumber, UIHelper.isHidpi() ? 20 : 10);
      StyleConstants.setForeground(linenumber, Color.GRAY);
      styles.put(LINE_NUMBER, linenumber);

      high_linenumber = document.addStyle(HIGH_LINE_NUMBER, normal);
      StyleConstants.setBold(high_linenumber, true);
      StyleConstants.setFontSize(high_linenumber, UIHelper.isHidpi() ? 20 : 10);
      StyleConstants.setForeground(high_linenumber, Color.BLUE);
      styles.put(HIGH_LINE_NUMBER, high_linenumber);

      skeyword = document.addStyle(KEYWORD, bold);
      styles.put(KEYWORD, skeyword);

      scurrent_key = document.addStyle(CURRENT_KEYWORD, bold);
      StyleConstants.setForeground(scurrent_key, Color.BLUE);
      styles.put(CURRENT_KEYWORD, scurrent_key);

      error_log = document.addStyle(ERROR_LOG, normal);
      StyleConstants.setForeground(error_log, Color.RED);
      styles.put(ERROR_LOG, error_log);

      predefinedHighlights.add(new Highlight("ERROR", false, ERROR_LOG));
   }

   private Highlight getNextHighlight(List<Highlight> keys, String logStat, String realLog, int startIndex)
   {
      Highlight found = null;
      for (Highlight key : keys)
      {
         int index = logStat.indexOf(key.getEffectiveKey(), startIndex);
         if (index < 0)
         {
            if (!key.isIgnoreCase())
            {
               index = realLog.indexOf(key.getEffectiveKey(), startIndex);
            }
         }
         if (index >= 0)
         {
            //got one
            if (found == null)
            {
               found = new Highlight(key, index);
            }
            else if (found.getIndex() > index)
            {
               found = new Highlight(key, index);
            }
         }
      }
      return found;
   }

   public StyledDocument formatText(String text, List<Highlight> highlights) throws BadLocationException
   {
      highlights.addAll(globalHighlights);
      highlights.addAll(predefinedHighlights);

      slices.clear();

      String textNoCase = text.toLowerCase();
      int start = 0;
      //int index = logNoCase.indexOf(keyNoCase, start);
      Highlight found = getNextHighlight(highlights, textNoCase, text, start);
      while (found != null)
      {
         int keyLen = found.getEffectiveKey().length();
         int index = found.getIndex();

         if (found.getIndex() > start)
         {
            String s = text.substring(start, index);
            slices.add(new StringSlice(s, normal));
         }
         String realKey = text.substring(index, index + keyLen);
         slices.add(new StringSlice(realKey, styles.get(found.getStyleName())));

         start = index + keyLen;
         found = getNextHighlight(highlights, textNoCase, text, start);
      }
      if (start < text.length())
      {
         //last piece
         String last = text.substring(start);
         slices.add(new StringSlice(last, normal));
      }

      for (StringSlice s : slices)
      {
         document.insertString(document.getLength(), s.str, s.style);
      }

      return document;
   }

   public static class StringSlice
   {
      public String str;
      public Style style;

      public StringSlice(String s, Style style)
      {
         this.str = s;
         this.style = style;
      }
   }

   public AttributeSet getStyle(String name)
   {
      return styles.get(name);
   }

   public static void addHighLight(String key, String styleName)
   {
      globalHighlights.add(new Highlight(key, true, styleName));
   }

   public static void clearHighLight()
   {
      globalHighlights.clear();
   }

}
