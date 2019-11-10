package org.apache.activemq.artemis.budapest.project.book;

public class PagingResult
{
   public long pageOffset = 0;
   public long index = 0;

   public PagingResult(long pageOffset2, long lineIndex)
   {
      this.pageOffset = pageOffset2;
      this.index = lineIndex;
   }

   public boolean isPageMoved()
   {
      return pageOffset != 0;
   }
}
