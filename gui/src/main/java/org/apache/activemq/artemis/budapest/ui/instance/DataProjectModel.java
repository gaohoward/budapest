package org.apache.activemq.artemis.budapest.ui.instance;

import org.apache.activemq.artemis.budapest.data.record.DataRecord;
import org.apache.activemq.artemis.budapest.project.AbstractProject;
import org.apache.activemq.artemis.budapest.project.book.DataBook;
import org.apache.activemq.artemis.budapest.project.book.LineLocation;
import org.apache.activemq.artemis.budapest.project.book.PageInfo;
import org.apache.activemq.artemis.budapest.project.book.PageOutOfScopeException;
import org.apache.activemq.artemis.budapest.project.book.PagingResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Executor;

public class DataProjectModel extends LogListModel
{
   private static final long serialVersionUID = 5520910871128345704L;
   private static final Logger logger = LoggerFactory.getLogger(DataProjectModel.class);
   private DataBook logBook;
   private long currentSelection = 0;

   public DataProjectModel(int pageSize, Executor pool)
   {
      super(pageSize);
      logBook = new DataBook(pageSize, pool);
   }

   public boolean isRootProject()
   {
      AbstractProject parent = logBook.getCurrentProject().getParent();
      return parent == null || !parent.supportIteration();
   }

   public void reset(AbstractProject project) throws Exception
   {
      logBook.openNew(project);
      updateModel();
   }

   public long gotoLine(long ln) throws Exception
   {
      logger.debug("model going to line: {}", ln);
      //page starts with 1
      PagingResult pr = logBook.toPageByLineNumber(ln);

      logger.debug("page offset: " + pr.pageOffset + " index; " + pr.index);
      boolean pageMoved = pr.isPageMoved();
      if (pageMoved)
      {
         logger.debug("update model...");
         updateModel();
      }
      boolean selectionChanged = setSelection(pr.index);

      if (pageMoved || selectionChanged)
      {
         return pr.index;
      }
      return -1;
   }

   public boolean setSelection(long index)
   {
      //use AtomicInteger.compareAndSet()?
      //if index is -1 means no selection.
      if (index != this.currentSelection)
      {
         this.currentSelection = index;
         return true;
      }
      return false;
   }

   public long locateEntry(String key, boolean forward) throws Exception
   {
      logger.debug("locating, logBook: {}, forward: {}", logBook, forward);
      long index = 0;
      if (forward)
      {
         index = logBook.searchLine(key, forward, this.currentSelection + 1);
      }
      else
      {
         index = logBook.searchLine(key, forward, this.currentSelection);
      }
      if (index != -1)
      {
         updateModel();
         this.currentSelection = index;
         return index;
      }
      return -1;
   }

   public boolean nextPage() throws IOException
   {
      if (logBook.nextPage())
      {
         updateModel();
         return true;
      }
      return false;
   }

   private void updateModel() throws IOException
   {
      logger.debug("updating model now, logBook: {}.", logBook);
      ArrayList<DataRecord> records = logBook.getPageContent();
      this.addPage(records);
   }

   public boolean prevPage() throws IOException
   {
      if (logBook.prevPage())
      {
         updateModel();
         return true;
      }
      return false;
   }

   public long getCurrentLineNumber()
   {
      return logBook.getCurrentLineNumber(this.currentSelection);
   }

   public PageInfo getPageInfo()
   {
      long pg = logBook.getCurrentPageNumber();
      return new PageInfo(pg, this.currentSelection + 1);
   }

   public long getCurrentPageNumber()
   {
      return logBook.getCurrentPageNumber();
   }

   public long getLineNumber(int index)
   {
      return logBook.indexToLineNumber(index);
   }

   public LineLocation getBookmarkAtLine(long line) throws PageOutOfScopeException
   {
      return logBook.bookMarkAtLine(line);
   }

   public long getLastLineNumber()
   {
      return logBook.getLastLineNumber();
   }

   public void tryGetTotalLine(String[] logSummary)
   {
      if (logBook.browseDone())
      {
         logSummary[2] = "Total lines: " + logBook.getLastLineNumber();
      }
      else
      {
         logSummary[2] = "Total lines: (Reading in progress...current: " + logBook.getLastLineNumber() + ")";
      }
   }
}

