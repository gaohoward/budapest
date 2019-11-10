package org.apache.activemq.artemis.budapest.project;

public class FilterString
{
   public static final int SIMPLE_EX = 0;
   public static final int REG_EX = 1;

   public final String ex;
   public final int filterType;

   public FilterString(String filter, int type)
   {
      this.ex = filter;
      this.filterType = type;
   }

   public FilterString(String filter)
   {
      this(filter, SIMPLE_EX);
   }

   @Override
   public int hashCode()
   {
      return ex.hashCode();
   }

   @Override
   public boolean equals(Object other)
   {
      if (other instanceof FilterString)
      {
         FilterString fs = (FilterString) other;
         if (filterType != fs.filterType)
         {
            return false;
         }
         if (ex.equals(fs.ex))
         {
            return true;
         }
      }
      return false;
   }

   @Override
   public String toString()
   {
      return this.ex;
   }

   public String getName()
   {
      return filterType == REG_EX ? "Regex: " + ex : ex;
   }

}
