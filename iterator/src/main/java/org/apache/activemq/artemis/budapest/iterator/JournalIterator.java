package org.apache.activemq.artemis.budapest.iterator;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JournalIterator<T> implements Iterator<T>
{
   private static final Logger logger = LoggerFactory.getLogger(JournalIterator.class);
   private JournalStore<T> store;
   private Cursor cursor = null;
   private JournalFileBrowser browser;

   public JournalIterator(JournalStore<T> journalReader)
   {
      this(journalReader, 4096);
   }

   public JournalIterator(JournalStore<T> journalReader, int internalCacheSize)
   {
      this.store = journalReader;
      this.browser = new JournalFileBrowser(internalCacheSize);
      try
      {
         init();
         logger.debug("Initialized an iterator with cache size: {}", internalCacheSize);
      }
      catch (IOException e)
      {
         throw new IllegalStateException("Failed to init iterator", e);
      }
   }

   //journal should be opened already
   public byte byteAt(JournalFile journal, long pos) throws IOException
   {
     long curPos = journal.position();
     try
     {
        journal.position(pos);
        ByteBuffer buffer = ByteBuffer.allocate(1);
        if (journal.read(buffer) != 1)
        {
          throw new IllegalStateException("Error reading the byte from " + journal + " position " + pos);
        }
        buffer.flip();
        return buffer.get();
     }
     finally
     {
       //restore position
       journal.position(curPos);
     }
   }

   //debug
   public String bytesAt(Bookmark bm, int n) throws IOException
   {
      StringBuilder builder = new StringBuilder("[");
      JournalFile journal = store.getJournalFile(bm.index);
      journal.open("r");
      ByteBuffer buffer = ByteBuffer.allocate(n);
      try
      {
         journal.position(bm.position);
         int read = journal.read(buffer);
         logger.info("bytes read: {}", read);
         buffer.flip();
         for (int i = 0; i < read; i++)
         {
            builder.append(ByteUtil.toIntValue(buffer.get()));
            builder.append(",");
         }
         builder.append("]");
      }
      finally
      {
         journal.close();
      }
      return builder.toString();
   }

   //debug
   public String status()
   {
     return "[Cursor]:" + (cursor == null ? "null" : cursor.status()) + "[Browser]:" + browser.status();
   }
   //current position of read
   public Bookmark position() throws IOException
   {
     return browser.getCurrentPosition();
   }
   
   public void reset(Bookmark bm) throws IOException
   {
     this.store.reset();
     this.browser = new JournalFileBrowser(this.browser.cacheSize, bm);
     this.browser.setPosition(bm);
     this.cursor = null;
     init();
   }

   @Override
   public boolean hasNext()
   {
      return cursor.hasNext();
   }
   
   public boolean hasPrev()
   {
      return cursor.hasPrev();
   }

   private void init() throws IOException
   {
      if (cursor == null)
      {
         cursor = new Cursor();
         browser.next(cursor);
      }
   }

   @Override
   public String toString()
   {
      return "[JournalIterator]" + System.identityHashCode(this) + ": iter: " + (cursor == null ? "uninitialized" : cursor.toString());
   }

   public T prev()
   {
      T realRecord = null;
      InternalRecord<T> record = null;
      try
      {
         if (cursor.changingDirection(Cursor.DIR_BACKWARD))
         {
            logger.debug("we need to change direction on browser");
            browser.reverseDirection(cursor);
         }
         record = browser.prev(cursor);
         if (record != null)
         {
            realRecord = record.record;
         }
      }
      catch (IOException e)
      {
         throw new IllegalStateException(e);
      }
      return realRecord;
   }

   @Override
   public T next()
   {
      T realRecord = null;
      InternalRecord<T> record = null;
      try
      {
         if (cursor.changingDirection(Cursor.DIR_FORWARD))
         {
            browser.reverseDirection(cursor);
         }
         record = browser.next(cursor);
         if (record != null)
         {
            realRecord = record.record;
         }
      }
      catch (IOException e)
      {
         throw new IllegalStateException(e);
      }
      return realRecord;
   }

   @Override
   public void remove()
   {
      // TODO Auto-generated method stub
   }

   private class JournalFileBrowser
   {
      private int cacheSize = 4096;
      private InternalRecord<T>[] cache = new InternalRecord[this.cacheSize];
      private int lastCacheRecordIndex = 0;
      private int cacheIndex = -1;
      private JournalFileCursor fileCursor;

      public JournalFileBrowser(int cacheSize, Bookmark initBm)
      {
         this.cacheSize = cacheSize;
         fileCursor = new JournalFileCursor(initBm);
      }

      public JournalFileBrowser(int cacheSize)
      {
         this(cacheSize, null);
      }

      public String status()
      {
        return "cacheSize:" + cacheSize + ",lastCacheIndex:" + lastCacheRecordIndex + ", cacheIndex:" + cacheIndex + ", fileCursor:" + fileCursor.status();
      }

      public Bookmark getCurrentPosition() throws IOException
      {
        return fileCursor.currentPosition();
      }
      
      public void setPosition(Bookmark bm) throws IOException
      {
        fileCursor.locate(bm);
      }

      //ToDo try move this to fileCursor
      public InternalRecord<T> next(Cursor holder) throws IOException
      {
         InternalRecord<T> current = holder.nextRecord();

         if (cacheIndex == -1)
         {
            logger.debug(JournalIterator.this + " now cacheIndex is -1 so load cache first, holder: {}, status: {}", holder, this.status());
            int numRecords = loadCache(true);

            logger.debug("{} loaded records: {}", JournalIterator.this, numRecords);
            if (numRecords == 0)
            {
               //no more
               holder.nextValue(null);

               return current;
            }
            lastCacheRecordIndex = numRecords -1;
            cacheIndex = 0;
         }

         holder.nextValue(cache[cacheIndex]);
         cacheIndex++;

         if (cacheIndex > lastCacheRecordIndex)
         {
            cacheIndex = -1;
         }
         
         return current;
      }

      //reverse the holder for opposite direction iteration.
      //this method need to reverse the holder's prev and next
      //in order for the iteration to proceed in the new direction.
      public void reverseDirection(Cursor cursor) throws IOException
      {
         if (cursor.reverseDirection() == Cursor.DIR_FORWARD)
         {
            changeCacheForward(cursor);               
         }
         else
         {
            changeCacheBackward(cursor);
         }
      }

      //Todo: try move this to JournalFileCursor
      private void changeCacheForward(Cursor cur) throws IOException
      {
         int num = 3;

         //before move check special case
         if (cacheIndex == -1)
         {
            //which means in the cache
            cacheIndex = 0;
            if (cur.atBeginning())
            {
               num = 1;
            }
            else
            {
               num = 2;//because we manually change cacheIndex to 0, so num should be 2 now.
            }
         }

         for (int i = 0; i < num; i++)
         {
            cacheIndex++;

            if (cacheIndex > lastCacheRecordIndex)
            {
               int numRecords = loadCache(true);

               if (numRecords == 0)
               {
                  //no more
                  return;
               }
               lastCacheRecordIndex = numRecords - 1;
               cacheIndex = 0;
            }
         }
      }

      //Todo: do we need atEnd? maybe check cacheIndex == -1 is enough.
      private void changeCacheBackward(Cursor cur) throws IOException
      {
         logger.debug("changeing cache backward ...");
         int num = 3;

         if (cacheIndex == -1)
         {
            cacheIndex = lastCacheRecordIndex;
            logger.debug("we are at the -1, make cacheIndex be last: {}", cacheIndex);
            
            if (cur.atEnd())
            {
               num = 1;
            }
            else
            {
               num = 2;
            }
         }

         logger.debug("num: {}, cacheIndex: {}", num, cacheIndex);
         for (int i = 0; i < num; i++)
         {
            cacheIndex--;
            if (cacheIndex == -1)
            {
               int numRecords = loadCache(false);

               if (numRecords == 0)
               {
                  //no more
                  return;
               }
               lastCacheRecordIndex = numRecords - 1;
               cacheIndex = lastCacheRecordIndex;
            }
         }
         logger.debug("changeCachebackward end, cacheIndex: {}", cacheIndex);
      }

      public InternalRecord<T> prev(Cursor holder) throws IOException
      {
         InternalRecord<T> current = holder.prevRecord();

         if (cacheIndex == -1)
         {
            logger.debug("cache index is -1, so load cache..., holder: {}", holder);
            int numRecords = loadCache(false);
            logger.debug("prev loaded cache: {}, holder: {}", numRecords, holder);

            if (numRecords == 0)
            {
               //no more
               holder.prevValue(null);
               return current;
            }
            lastCacheRecordIndex = numRecords - 1;
            cacheIndex = lastCacheRecordIndex;
         }
         holder.prevValue(cache[cacheIndex]);
         cacheIndex--;
         return current;
      }

      private int loadCache(boolean forward) throws IOException
      {
         logger.debug("loading cache, forward? {}", forward);
         if (forward)
         {
            return fileCursor.forward(cache);
         }
         else
         {
            return fileCursor.backward(cache);
         }
      }
   }

   private class InternalRecord<Y>
   {
      public Bookmark position;
      public Y record;

      public InternalRecord(Bookmark mark, Y rec)
      {
         position = mark;
         record = rec;
      }
      
      public String toString()
      {
         return "[(" + position + ")" + record + "]";
      }
   }

   private class Cursor
   {
      public static final int DIR_FORWARD = 1;
      public static final int DIR_BACKWARD = -1;

      private InternalRecord<T> next = null;
      private InternalRecord<T> prev = null;
      private int currentDirection = DIR_FORWARD;

      public String toString()
      {
         return prev + (currentDirection == 1 ? " => " : " <= ") + next;
      }

      public String status()
      {
        return toString();
      }

      public boolean atBeginning()
      {
         return (prev == null) && (next != null);
      }

      public boolean atEnd()
      {
         return (prev != null) && (next == null);
      }

      //return new direction
      public int reverseDirection()
      {
         if (currentDirection == DIR_FORWARD)
         {
            currentDirection = DIR_BACKWARD;
         }
         else
         {
            currentDirection = DIR_FORWARD;
         }
         return currentDirection;
      }

      public boolean changingDirection(int direction)
      {
         if (currentDirection != direction)
         {
            return true;
         }
         return false;
      }
      
      public boolean hasPrev()
      {
         return prev != null;
      }
      
      public boolean hasNext()
      {
         return next != null;
      }

      public InternalRecord<T> nextRecord()
      {
         return next;
      }

      public InternalRecord<T> prevRecord()
      {
         return prev;
      }
      
      public void nextValue(InternalRecord<T> record)
      {
         if (record == null && this.next == null) return;
         this.prev = next;
         this.next = record;
      }
      
      public void prevValue(InternalRecord<T> record)
      {
         if (record == null && this.prev == null) return;
         this.next = prev;
         this.prev = record;
      }
   }

   //a class helps traverse through a journal file
   private class JournalFileCursor extends InputStream
   {
      private int index = -1;
      private JournalFile current;

      private ByteBuffer buffer = ByteBuffer.allocate(4096);
      private boolean active = false;
      private JournalInputStream stream;
      
      //the tracker tracks the cursor movement.
      //it keeps record of bookmarks of all read pages
      //(including index of the file and its channel positions)
      //it also has a pointer to track current page
      //when forward(), it checks if the pointer points to
      //the last, if not, move the pointer forward and init
      //the cursor before forward. If its the last,
      //record the pointer and forward.
      //when backward, it moves back the pointer and 
      //init the cursor before backward().
      private BrowseHistory tracker;

      public JournalFileCursor(Bookmark initBm)
      {
         this.tracker = new BrowseHistory(initBm);
      }

      void locate(Bookmark bk) throws IOException
      {
         closeStream();
         index = bk.index;
         if (index < store.size())
         {
            current = store.getJournalFile(index);
            current.open("r");
            current.position(bk.position);
            buffer.position(buffer.limit());
            active = true;
         }
      }
      
      public String status()
      {
        Bookmark pos = null;
        try
        {
          pos = currentPosition();
        }
        catch (IOException e)
        {
          logger.error("got exception", e);
        }
        return this + " current position: " + pos;
      }

      public Bookmark currentPosition() throws IOException
      {
        if (stream != null)
        {
          return stream.getCurrentPosition();
        }
        return null;
      }

      //cache should be of same size during forward/backward
      public int forward(InternalRecord<T>[] cache) throws IOException
      {
         logger.debug("forward loading cache of size: {}", cache.length);
         boolean forwarded = tracker.forward();
         
         logger.debug("tracker forward result: {}", forwarded);

         int len = cache.length;
         int totalRead = 0;
         Bookmark last = null;

         while (totalRead < len)
         {
            InternalRecord<T> record = null;
            try
            {
               JournalInputStream inputStream = getJournalInputStream();
               Bookmark pos = inputStream.getCurrentPosition();
               
               if (pos == null)
               {
                 break;
               }

               T rec = store.getJournalRecordDecoder().decode(inputStream);

               if (rec == null)
               {
                 break;
               }

               //is rec != null redundant check??
               if (rec != null && (!forwarded))
               {
                  //fix it: if the rec is last, and eof is reached,
                  //inputStream will have been closed now. 
                  //current.position will throw exception.
                  last = new Bookmark(index, current.position() - buffer.limit() + buffer.position());
               }

               record = new InternalRecord<T>(pos, rec);

            }
            catch (DecodeError e)
            {
               throw new IOException("Error Decoding", e);
            }

            cache[totalRead] = record;
            totalRead++;
         }

         if ((!forwarded) && (totalRead > 0))
         {
            tracker.addBookmark(last);
         }
         logger.debug("{} records have been loaded into cache.", totalRead);
         return totalRead;
      }

      public int backward(InternalRecord<T>[] cache) throws IOException
      {
         boolean backwarded = tracker.backward();

         if (!backwarded) return 0;
         
         int len = cache.length;
         int totalRead = 0;

         while (totalRead < len)
         {
            InternalRecord<T> record = null;
            try
            {
               JournalInputStream inputStream = getJournalInputStream();
               Bookmark pos = inputStream.getCurrentPosition();

               T rec = store.getJournalRecordDecoder().decode(inputStream);
               if (rec == null) break;
               record = new InternalRecord<T>(pos, rec);
            }
            catch (DecodeError e)
            {
               throw new IOException("Error decoding", e);
            }

            cache[totalRead] = record;
            totalRead++;
         }

         logger.debug("{} records have been loaded into cache.", totalRead);
         return totalRead;
      }

      private void closeStream() throws IOException
      {
         if (current != null)
         {
            current.close();
            active = false;
         }
      }

      private boolean tryAdvance() throws IOException
      {
         while (!active)
         {
            if (index == store.size() - 1)
            {
               return active;
            }

            index++;
            if (index < store.size())
            {
               current = store.getJournalFile(index);//should not direct access physical file
               current.open("r");
               buffer.position(0);
               int n = current.read(buffer);
               if (n == -1)
               {
                  //end of stream
                  closeStream();
                  continue;
               }
               buffer.position(0);
               buffer.limit(n);
               active = true;
            }
         }
         return active;
      }

      private LinkedList<ByteInfo> lastBytes = new LinkedList<ByteInfo>();
      private final int buffSize = 8;
      private boolean traceLastBytes = false;
      
      @Override
      public int read() throws IOException
      {
         try
         {
            final int n = readInternal();
            if (traceLastBytes)
            {
               ByteInfo info = null;
               if (n != -1)
               {
                  //Todo: this method is called for each byte, so
                  //store.getJournalFile(index).getFile().getName() should be cached
                  //refactor it!!
                  info = new ByteInfo(n, store.getJournalFile(index).getFile().getName(), current.position() - buffer.limit() + buffer.position() - 1);
               }
               else
               {
                  info = new ByteInfo(n, "Special: " + index, -1);
               }
               lastBytes.addLast(info);
               if (lastBytes.size() > buffSize)
               {
                  lastBytes.removeFirst();
               }
            }

            logger.trace("reading a raw byte: " + n);
            return n;
         }
         catch (IOException e)
         {
            e.printStackTrace();
            throw e;
         }
      }
      
      public int readInternal() throws IOException
      {
         while (tryAdvance())
         {
            if (buffer.position() < buffer.limit())
            {
               int val = 0x000000FF & buffer.get();
               return val;
            }
            //buffer is empty, read again
            buffer.position(0);
            buffer.limit(buffer.capacity());

            int n = current.read(buffer);

            if (n > 0)
            {
               buffer.position(0);
               buffer.limit(n);
               int val = 0x000000FF & buffer.get();

               return val;
            }
            if (n == -1)
            {
               closeStream();
            }
         }
         return -1;
      }
      
      private class BrowseHistory
      {
         List<Bookmark> history = new ArrayList<Bookmark>();
         int pointer = 0;
         boolean forwarding;
         Bookmark initBm;
         
         public BrowseHistory(Bookmark initBm)
         {
            forwarding = true;
            this.initBm = initBm;
         }

         //the new mark is the next reading point
         public void addBookmark(Bookmark mark)
         {
            history.add(mark);
            pointer = history.size() - 1;

            forwarding = true;
         }
         
         public boolean backward() throws IOException
         {
            if (forwarding)
            {
               pointer -= 2;
               forwarding = false;
            }
            if (pointer < 0) 
            {
               pointer = -1;
               return false;
            }
            
            Bookmark bookmark = history.get(pointer);
            locate(bookmark);
            pointer--;
            return true;
         }

         public boolean forward() throws IOException
         {
            if (!forwarding)
            {
               pointer += 2;
               forwarding = true;
            }

            if (history.size() == 0)
            {
               addBookmark(initBm == null ? new Bookmark(0, 0) : initBm);
               return false;
            }

            if (pointer == history.size() - 1)
            {
               return false;
            }

            Bookmark bookmark = history.get(pointer);

            locate(bookmark);
            pointer++;
            return true;
         }
         
         public String toString()
         {
            return "BrowseHistory, pointer: " + pointer + " History: " + history;
         }
      }
      
      private class ByteInfo
      {
         int value;
         String filePath;
         long pos;

         public ByteInfo(int v, String f, long p)
         {
            value = v;
            filePath = f;
            pos = p;
         }

         public String toString()
         {
            return "byte[" + value + "], file[" + filePath + "], pos[" + pos + "]";
         }
      }

      private JournalInputStream getJournalInputStream()
      {
         if (stream != null) return stream;
         stream = new JournalInputStream(this) {

            @Override
            public JournalFile getCurrentFile() {
              if (current == null) return null;
              return current;
            }

            @Override
            public ByteType verifyUnknownByte(int theByte) {
               if (current != null)
               {
                  return current.ignoreLastByte(theByte);
               }
               throw new IllegalStateException("No journal file for the byte: " + theByte);
            }

            @Override
            public Bookmark getCurrentPosition() throws IOException {
               if (current == null)
               {
                  return new Bookmark(0, 0);
               }
               
               if (!current.isOpen())
               {
                  //we assume it's in the journal end.
                  return null;
               }
               long pos = current.position() - buffer.limit() + buffer.position();
               return new Bookmark(index, pos);
            }
            
         };
         assert(buffer.position() == 0 || buffer.position() == buffer.limit());
         if (current != null)
         {
            try
            {
               store.initDecoder(current);
            }
            catch (IOException e)
            {
               logger.error("Failed to init decoder", e);
            }
         }
         return stream;
      }
   }
}