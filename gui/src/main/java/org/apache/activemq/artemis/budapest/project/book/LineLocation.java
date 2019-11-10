package org.apache.activemq.artemis.budapest.project.book;

import org.apache.activemq.artemis.budapest.iterator.Bookmark;

public class LineLocation
{
   public Bookmark bm;
   public long linesToMove;
   public long index;

   public LineLocation(Bookmark bm, long linesToMove, long lineIndex)
   {
      this.bm = bm;
      this.linesToMove = linesToMove;
      this.index = lineIndex;
   }

   @Override
   public String toString()
   {
      return "bm: " + bm + " linesToMove: " + linesToMove + " index: " + index;
   }
}
