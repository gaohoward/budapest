package org.apache.activemq.artemis.budapest.ui.instance;

public class Highlight
{
   private String keyword;
   private boolean ignoreCase;
   private String effectiveKey;
   private int index;
   private String styleName;

   @Override
   public int hashCode()
   {
      return effectiveKey.hashCode();
   }

   public boolean equals(Object another)
   {
      if (another instanceof Highlight)
      {
         Highlight highlight = (Highlight)another;
         return effectiveKey.equals(highlight.effectiveKey);
      }
      return false;
   }

   public String getEffectiveKey()
   {
      return effectiveKey;
   }

   public Highlight(String keyword, String styleName)
   {
      this(keyword, true, styleName);
   }

   public Highlight(String keyword, boolean ignoreCase, String styleName)
   {
      this.keyword = keyword;
      this.ignoreCase = ignoreCase;
      if (this.ignoreCase)
      {
         effectiveKey = keyword.toLowerCase();
      }
      else
      {
         effectiveKey = keyword;
      }
      this.styleName = styleName;
      index = -1;
   }

   public Highlight(Highlight key, int index)
   {
      this.keyword = key.keyword;
      this.ignoreCase = key.ignoreCase;
      this.effectiveKey = key.effectiveKey;
      this.index = index;
      this.styleName = key.styleName;
   }

   @Override
   public String toString()
   {
      return "keyword: " + keyword + " effective: " + effectiveKey;
   }

   public int getIndex()
   {
      return index;
   }

   public String getStyleName()
   {
      return this.styleName;
   }

   public boolean isIgnoreCase()
   {
      return this.ignoreCase;
   }
}
