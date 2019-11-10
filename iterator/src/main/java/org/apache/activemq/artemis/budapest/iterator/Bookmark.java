package org.apache.activemq.artemis.budapest.iterator;

public class Bookmark
{
   int index = -1;
   long position = 0;
   boolean isBegin;

   public Bookmark(int ind, long pos)
   {
      index = ind;
      position = pos;
      isBegin = (index == 0 && position == 0);
   }

   public boolean isBegin()
   {
      return isBegin;
   }
   
   @Override
   public boolean equals(Object obj)
   {
     if (obj instanceof Bookmark)
     {
       Bookmark other = (Bookmark)obj;
       if (this.index == other.index && this.position == other.position)
       {
         return true;
       }
     }
     return false;
   }

   // >=
   public boolean gtOrEq(Object obj)
   {
     if (this.equals(obj))
     {
       return true;
     }
     Bookmark other = (Bookmark)obj;

     if (this.index > other.index)
     {
       return true;
     }

     if (this.index == other.index)
     {
       return this.position >= other.position;
     }
     return false;
   }

   public String toString()
   {
      return "index: " + index + " pos: " + position;
   }
}
