package org.apache.activemq.artemis.pudapest.iterator.test;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;

import org.apache.activemq.artemis.budapest.iterator.JournalIterator;
import org.apache.activemq.artemis.budapest.iterator.JournalStore;
import org.apache.activemq.artemis.pudapest.iterator.test.util.SimpleJournalStore;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class IterationTest
{
   @Parameters
   public static Collection<Object[]> getParams()
   {
      return Arrays.asList(new Object[][] {
            {1024, 2, 0}, //0
            {10240, 2, 1},
            {1024, 2, 2},
            {1024, 2, 3},
            {1024, 2, 4},
            {1024, 2, 5},
            {1024, 2, 6},
            {1024, 2, 7},
            {1024, 2, 8},
            {1024, 2, 9},
            {1024, 2, 10},
            {1024, 10, 0},
            {1024, 10, 1},
            {1024, 10, 29},
            {1024, 10, 57},
            {10240, 10, 300},
            {20240, 10, 500},
            {10240, 10, 1000},
            {10230, 10, 1023},
            {10240, 10, 1024},//default cache size is 1024 records
            {10240, 10, 1025},
            {102400, 10, 2048},
            {102400, 10, 4096},
            {102400, 10, 8192},
            {102400, 20, 16384},
            {102400, 20, 32768},
            {102400, 20, 65536},
            {10240000, 10, 100000},
            {10240000, 10, 134233},
            {1024000, 30, 500000},
            {-1, 0, 0} //last one used to clean up data.
      });
   }

   private int journalFileSize;
   private int numJournalFiles;
   private int numRecords;

   private JournalStore store;

   public IterationTest(int journalFileSize, int numJournalFiles, int numRecords)
   {
      this.journalFileSize = journalFileSize;
      this.numJournalFiles = numJournalFiles;
      this.numRecords = numRecords;
   }

   @Before
   public void setUp() throws Exception
   {
      SimpleJournalStore.cleanStoreBaseDir();
      if (this.journalFileSize > 0)
      {
         String testJournalDir = "test_" + this.journalFileSize + "_" + this.numJournalFiles + "_" + this.numRecords;
         store = SimpleJournalStore.createStore(testJournalDir, journalFileSize, numJournalFiles, numRecords);
      }
   }

   @Test
   public void testIteration() throws Exception
   {
      if (this.journalFileSize < 0) return;
      System.out.println("##################### Test (" + this.journalFileSize + ", " + this.numJournalFiles + ", " + this.numRecords + ")");

      //the tests are best done with 1024.
      JournalIterator iter = new JournalIterator(store, 1024);

      //first verify hasPrev and hasNext
      assertEquals("Initially the one record store should have next", iter.hasNext(), numRecords > 0);
      assertFalse("Buf no prev", iter.hasPrev());

      //iterate forward
      int num = 0;
      Object next = null;
      while (iter.hasNext())
      {
         next = iter.next();
         assertNotNull(next);
         num++;
      }

      assertEquals(numRecords, num);
      
      assertEquals("as long as numRec is not zero, hasPrev() should be true", numRecords > 0, iter.hasPrev());

      //now backward
      num = 0;
      Object prev = null;
      while (iter.hasPrev())
      {
         prev = iter.prev();
         assertNotNull(prev);
         num++;
      }
      assertEquals(numRecords, num);
      
      //now we back to the beginning
      assertEquals("back to the beginning", numRecords > 0, iter.hasNext());
      
      //second round of iteration
      num = 0;
      while (iter.hasNext())
      {
         next = iter.next();
         assertNotNull(next);
         num++;
      }

      assertEquals(numRecords, num);

      num = 0;
      while (iter.hasPrev())
      {
         prev = iter.prev();
         assertNotNull(prev);
         num++;
      }
      assertEquals(numRecords, num);
   }
}
