package org.apache.activemq.artemis.budapest.project.book;

import org.apache.activemq.artemis.budapest.data.record.DataRecord;
import org.apache.activemq.artemis.budapest.iterator.Bookmark;
import org.apache.activemq.artemis.budapest.iterator.JournalIterator;
import org.apache.activemq.artemis.budapest.project.AbstractProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

public class DataBook
{
   private static final Logger logger = LoggerFactory.getLogger(DataBook.class);
   // max lines in a page.
   private int pageCapacity;

   private long currentPageNumber;
   private ArrayList<DataRecord> pageCache;

   private AbstractProject currentProject;
   //logIter should always stays at the beginning of a page.
   private JournalIterator<? extends DataRecord> logIter;

   private List<Bookmark> bookmarks = new ArrayList<Bookmark>();
   private BookReader reader = new BookReader();
   private AtomicBoolean empty = new AtomicBoolean(false);

   // number of pages between bookmarks
   private int bookmarkStep = 10;

   private Executor threadPool;

   public DataBook(int pageCapacity, Executor pool)
   {
      this.pageCapacity = pageCapacity;
      this.threadPool = pool;
      pageCache = new ArrayList<DataRecord>();
      reset();
   }

   private void reset()
   {
      pageCache.clear();
      currentPageNumber = 0;
      currentProject = null;
      logIter = null;
   }

   /*
    * Open a log project and kick off reader
    * Automatically go to page 1.
    */
   public void openNew(AbstractProject project) throws Exception
   {
      logger.debug("opening a new project book: {}", project);
      if (currentProject != null)
      {
         currentProject.close();
      }
      reader.stop();
      reset();

      currentProject = project;
      currentProject.open();
      logIter = currentProject.iterator();
      reader = new BookReader();
      reader.start();
      this.toPage(1);
   }

   public void close()
   {
      reader.stop();
   }

   public AbstractProject getCurrentProject()
   {
      return currentProject;
   }

   public boolean toPage(long pgNum) throws Exception
   {
      return toPage(pgNum, false);
   }

   public long getLastLineNumber()
   {
      return this.reader.lastLine;
   }
   //to the beginning of a page
   //return true if current page really changed.
   //note: page num starts from 1
   public boolean toPage(long pgNum, boolean force) throws Exception
   {
      if (pgNum < 1)
      {
         throw new IllegalArgumentException("Page number cannot be less than 1 : " + pgNum);
      }
      logger.debug("turning to page {}, current: {}, force: {}", pgNum, this.currentPageNumber, force);
      if (this.currentPageNumber == pgNum && !force)
      {
         return false;
      }
      reader.waitForPage(pgNum);
      if (empty.get())
      {
         return true;
      }
      //bookmark
      long bmIndex = (pgNum - 1) / this.bookmarkStep;
      logger.debug("we got bookmark for page: {}", bmIndex);
      //offset from the bookmark
      long offset = pgNum - bmIndex * this.bookmarkStep - 1;
      long linesToMove = offset * this.pageCapacity;
      logger.debug("we need to move to {} pages away from bookmark {}, total {} lines to move.", offset, bmIndex, linesToMove);
      //now reset iterator
      Bookmark bm = bookmarks.get((int)bmIndex);
      logger.debug("fetched bookmark {} and set iterator to it.", bm);
      logIter.reset(bm);
      //now iterating
      while (logIter.hasNext() && (linesToMove > 0))
      {
         logIter.next();
         --linesToMove;
      }
      this.currentPageNumber = pgNum;
      this.fetchPageContent();
      return true;
   }

   private void addBookmark(Bookmark bm)
   {
      synchronized (bookmarks)
      {
         logger.debug("adding a bookmark: {}", bm);
         bookmarks.add(bm);
         bookmarks.notifyAll();
      }
   }

   private class BookReader implements Runnable
   {
      private volatile boolean abort;
      private AtomicBoolean readDone = new AtomicBoolean(false);

      private volatile long lastPage;
      private volatile long lastLine;

      public BookReader()
      {
         abort = false;
         readDone.set(false);
         lastPage = 0;
         lastLine = 0;
      }

      public void stop()
      {
         abort = true;
         lastPage = 0;
         lastLine = 0;
         synchronized (readDone)
         {
            readDone.notifyAll();
         }
      }

      public void start()
      {
         threadPool.execute(this);
      }

      public void waitForPage(long pgNo) throws PageOutOfScopeException
      {
         if (lastPage > pgNo)
         {
            return;
         }
         //now wait
         synchronized (readDone)
         {
            while (pgNo > lastPage && (!abort) && (!readDone.get()))
            {
               try
               {
                  readDone.wait(50);
               }
               catch (InterruptedException e)
               {
                  logger.debug("waitForLine is interrupted");
               }
            }
         }
         if (readDone.get() && lastPage == 0)
         {
            empty.compareAndSet(false, true);
            if (pgNo == 1)
            {
               //only allow to page 1.
               return;
            }
         }
         if (abort || pgNo <= lastPage)
         {
            logger.debug("wait for page {} done. Now last page in reader: {}", pgNo, lastPage);
            return;
         }
         logger.error("Target page {} beyond limit {}", pgNo, lastPage);
         throw new PageOutOfScopeException("Page out of scope");
      }

      @Override
      public void run()
      {
         logger.debug("Kicking off reader thread...");
         try
         {
            JournalIterator<? extends DataRecord> iter = currentProject.iterator();

            while (!abort && !readDone.get())
            {
               DataRecord firstRecord = null;
               // read n pages
               for (int i = 0; i < bookmarkStep; i++)
               {
                  logger.debug("in leaping stage {} ...", i);
                  int lines = 0;
                  while (iter.hasNext())
                  {
                     if (firstRecord == null)
                     {
                        firstRecord = iter.next();
                        addBookmark(firstRecord.position());
                     }
                     else
                     {
                        iter.next();
                     }
                     lines++;
                     if (lines == pageCapacity)
                     {
                        // a new page
                        break;
                     }
                  }
                  // either a full page
                  // or the last page (maybe less than a full page)
                  if (lines > 0)
                  {
                     lastPage++;
                     lastLine += lines;
                  }

                  if (!iter.hasNext())
                  {
                     // end of book
                     readDone.set(true);
                     logger.debug("now reader done, lastpage: {}, lastline: {}", lastPage, lastLine);
                     synchronized (readDone)
                     {
                        readDone.notifyAll();
                     }
                     break;
                  }
               }
            }
         }
         catch (Exception e)
         {
            // log
            logger.error("Got exception", e);
         }
      }

      public boolean waitForFinish()
      {
         while (!readDone.get())
         {
            synchronized (readDone)
            {
               try
               {
                  readDone.wait(20);
                  if (abort) break;
               }
               catch (InterruptedException e)
               {
               }
            }
         }
         return readDone.get();
      }

      public long getTotalPages()
      {
         if (this.waitForFinish())
         {
            return this.lastPage;
         }
         logger.warn("failed to get total page, aborted: " + reader.abort);
         return 0;
      }

      public long getTotalLines()
      {
         if (this.waitForFinish())
         {
            return this.lastLine;
         }
         logger.warn("failed to get total lines, aborted: " + reader.abort);
         return 0;
      }
   }

   //starting from logIterator's current position
   public long searchLine(String key, boolean forward, long index) throws Exception
   {
      logger.debug("searching line by key: {}, forward? {}, index: {} ", key, forward, index);

      if (index >= this.pageCache.size() || index < 0) return -1;

      if (forward)
      {
         return searchForward(key, index);
      }
      else
      {
         return searchBackward(key, index);
      }
   }

   private long searchForward(String key, long index) throws Exception
   {
      logger.debug("searching forward key: {}, index: {}", key, index);;
      long originPg = this.currentPageNumber;
      boolean result = false;
      toCurrentLine(index);
      long targetLineNumber = this.getCurrentLineNumber(index);
      long tmpLineNumber = targetLineNumber;

      while (logIter.hasNext())
      {
         DataRecord rec = logIter.next();
         logger.trace("checking line: {}", rec.getContent());
         if (rec.match(key))
         {
            result = true;
            break;
         }
         ++ tmpLineNumber;
      }
      if (result)
      {
         //update page number
         long newPg = (tmpLineNumber - 1) / this.pageCapacity + 1;
         this.toPage(newPg, true);
         long sindex = tmpLineNumber - (this.currentPageNumber - 1) * this.pageCapacity - 1;
         return sindex;
      }

      //restore position, no page changed.
      logger.debug("no entry found, go back to originl page: " + originPg);
      this.toPage(originPg, true);
      return -1;
   }

   private long searchBackward(String key, long index) throws Exception
   {
      long originPg = this.currentPageNumber;
      toCurrentLine(index);
      long targetLineNumber = this.getCurrentLineNumber(index);
      long tmpLineNumber = targetLineNumber;

      while (hasPrev(tmpLineNumber))
      {
         -- tmpLineNumber;
         DataRecord rec = logIter.prev();
         if (rec.match(key))
         {
            logger.debug("Match at line: {}", tmpLineNumber);
            long newPg = (tmpLineNumber - 1) / this.pageCapacity + 1;
            this.toPage(newPg, true);
            long sindex = tmpLineNumber - (this.currentPageNumber - 1) * this.pageCapacity - 1;
            return sindex;
         }
         logger.debug("no match, continue...");
      }

      //restore position, no page changed.
      this.toPage(originPg, true);
      return -1;
   }

   private boolean hasPrev(long currentLine) throws IOException
   {
      if (logIter.hasPrev()) return true;
      if (currentLine == 1) return false;
      //we are at boundary, move up and adjust the logIter
      //first calculate curr page:
      this.currentPageNumber = (currentLine - 1) / this.pageCapacity + 1;
      logger.debug("we are at boundary, current page should be: " + this.currentPageNumber);
      int bmIndex = (int) (this.currentPageNumber - 1) / this.bookmarkStep - 1;
      logger.debug("we got last bm index: " + bmIndex);
      Bookmark bm = this.bookmarks.get(bmIndex);
      logIter.reset(bm);
      //now move to (bookmarkStep - 1)th page
      int moves = this.bookmarkStep * this.pageCapacity;
      logger.debug("move from bm " + moves + " records");
      for (int i = 0; i < moves; i++)
      {
         logIter.next();
      }
      return true;
   }

   //move iterator to current page top
   //don't change current line number!
   @SuppressWarnings("unused")
   private void toPageTop(int moves)
   {
      for (int i = 0; i < moves; i++)
      {
         DataRecord rec = logIter.prev();
         logger.debug("back a record: [{}] {}", i, rec.getContent());
         if (rec == null)
         {
            logger.error("got null when doing toPageTop, your algrithm is wrong!");
            logger.error("current page: {}, current Line {}", this.currentPageNumber, this.getCurrentLineNumber(0));
            throw new IllegalStateException("got null when doing toPageTop, your algrithm is wrong!");
         }
      }
   }

   //move logIter to the current line.
   //normally logIter stays at page top.
   //however if user is doing directional search
   //we need to move it to the search point which
   //is current line
   private void toCurrentLine(long index)
   {
      //calculate offset
      for (long i = 0; i < index; i++)
      {
         logIter.next();
      }
   }

   //next page
   //we don't use toPage() for performance reason
   public boolean nextPage() throws IOException
   {
      long nextPageNumer = this.currentPageNumber + 1;
      try
      {
         reader.waitForPage(nextPageNumer);
      }
      catch (PageOutOfScopeException e)
      {
         //we already at last
         return false;
      }

      //note iterator always stay at page top
      pageDown(false);
      int deviation = pageDown(true);

      this.toPageTop(deviation);
      this.currentPageNumber++;
      return true;
   }

   private int pageDown(boolean fillCache)
   {
      if (fillCache)
      {
         this.pageCache.clear();
      }
      int deviation = 0;
      while (logIter.hasNext())
      {
         DataRecord rec = logIter.next();
         if (fillCache)
         {
            this.pageCache.add(rec);
         }
         ++ deviation;
         if (deviation == this.pageCapacity)
         {
            break;
         }
      }
      return deviation;
   }

   public boolean prevPage() throws IOException
   {
      if (this.currentPageNumber == 1)
      {
         return false;
      }
      int deviation = 0;
      while (logIter.hasPrev())
      {
         logIter.prev();
         ++ deviation;
         if (deviation == this.pageCapacity)
         {
            break;
         }
      }
      if (deviation == 0)
      {
         //now iterator is at bookmarkStep boundary
         logger.debug("we are at boundary, current page: " + currentPageNumber);
         int bmIndex = (int) (currentPageNumber - 1) / this.bookmarkStep - 1;
         logger.debug("we got last bm index: " + bmIndex);
         Bookmark bm = this.bookmarks.get(bmIndex);
         logIter.reset(bm);
         //now move to (bookmarkStep - 1)th page
         int moves = (this.bookmarkStep - 1) * this.pageCapacity;
         for (int i = 0; i < moves; i++)
         {
            logIter.next();
         }
      }
      this.currentPageNumber--;
      //here we use fetch, a bit inefficient
      //but no need to worry about order.
      this.fetchPageContent();
      return true;
   }

   //index is 0 -- (pageCapacity - 1)
   public long getCurrentLineNumber(long index)
   {
      return (this.currentPageNumber - 1) * this.pageCapacity + index + 1;
   }

   public void fetchPageContent() throws IOException
   {
      pageCache.clear();
      while (logIter.hasNext())
      {
         DataRecord rec = logIter.next();
         pageCache.add(rec);
         if (pageCache.size() == this.pageCapacity)
         {
            break;
         }
      }
      toPageTop(pageCache.size());
   }

   public long getCurrentPageNumber()
   {
      return this.currentPageNumber;
   }

   public long indexToLineNumber(int index)
   {
      return (this.currentPageNumber - 1) * this.pageCapacity + index + 1;
   }

   //move to the page that has the line
   //return page distance and index of the line
   //-1 means to the end
   public PagingResult toPageByLineNumber(long ln) throws Exception
   {
      if (ln == -1)
      {
         //to the page end.
         long linex = reader.getTotalLines();
         return toPageByLineNumber(linex);
      }

      long targetPgNum = (ln - 1) / this.pageCapacity + 1;
      long pageOffset = targetPgNum - this.currentPageNumber;
      this.toPage(targetPgNum);
      long lineIndex = (ln - 1) % this.pageCapacity; //index always starts with 0

      return new PagingResult(pageOffset, lineIndex);
   }

   //Note the content should be always sync
   //when this method is called. In other words,
   //each paging should end with fetchPageContent()!
   public ArrayList<DataRecord> getPageContent()
   {
      return this.pageCache;
   }

   public int getPageCapacity()
   {
      return this.pageCapacity;
   }

   public void setPageCapacity(int newSize)
   {
      this.pageCapacity = newSize;
   }

   public long getTotalPages()
   {
      return reader.getTotalPages();
   }

   public long getTotalLines()
   {
      return reader.getTotalLines();
   }

   public LineLocation bookMarkAtLine(long line) throws PageOutOfScopeException
   {
      long targetPgNum = (line - 1) / this.pageCapacity + 1;
      reader.waitForPage(targetPgNum);
      long bmIndex = (targetPgNum - 1) / this.bookmarkStep;
      Bookmark bm = bookmarks.get((int)bmIndex);
      long lineIndex = (line - 1) % this.pageCapacity; //index always starts with 0

      long offset = targetPgNum - bmIndex * this.bookmarkStep - 1;
      long linesToMove = offset * this.pageCapacity;

      return new LineLocation(bm, linesToMove, lineIndex);
   }

   public boolean browseDone()
   {
      return reader.readDone.get();
   }
}
