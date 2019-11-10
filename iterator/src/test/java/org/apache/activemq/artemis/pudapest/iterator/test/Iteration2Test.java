package org.apache.activemq.artemis.pudapest.iterator.test;

import static org.junit.Assert.*;

import java.util.Random;

import org.apache.activemq.artemis.budapest.iterator.JournalIterator;
import org.apache.activemq.artemis.pudapest.iterator.test.util.SimpleJournalStore;
import org.apache.activemq.artemis.pudapest.iterator.test.util.SimpleRecord;
import org.junit.Before;
import org.junit.Test;

public class Iteration2Test
{

   @Before
   public void setUp() throws Exception
   {
      SimpleJournalStore.cleanStoreBaseDir();
   }
   
   public static String iteratorDesc()
   {
      return null;
   }

   @Test
   public void testFreeIteration() throws Exception
   {
      Random r = new Random(System.currentTimeMillis());
      int numRec = 10;

      SimpleJournalStore store = SimpleJournalStore.createStore("testFreeIteration", 2014, 20, numRec);

      JournalIterator iter = new JournalIterator(store);

      SimpleRecord record = null;
      for (int i = 0; i < numRec; i++)
      {
         record = (SimpleRecord) iter.next();
         assertTrue("should have id " + i + " in " + record, record.getMessage().contains("i" + i + "i"));
      }

      for (int i = 0; i < numRec; i++)
      {
         record = (SimpleRecord) iter.prev();
         int id = numRec - 1 - i;
         assertNotNull("null at " + i, record);
         assertTrue("should have id " + id + " in " + record, record.getMessage().contains("i" + id + "i"));
      }

      //half way return
      for (int k = 0; k < numRec; k++)
      {
         int stopPoint = numRec - k;
         for (int i = 0; i < stopPoint; i++)
         {
            record = (SimpleRecord) iter.next();
            assertNotNull("null at " + i, record);
            assertTrue("should have id " + i + " in " + record, record.getMessage().contains("i" + i + "i"));         
         }

         for (int i = 0; i < stopPoint; i++)
         {
            record = (SimpleRecord) iter.prev();
            int id = stopPoint - i - 1;
            assertTrue(i + " - should have id " + id + " in " + record, record.getMessage().contains("i" + id + "i"));
         }

         for (int i = 0; i < stopPoint; i++)
         {
            record = (SimpleRecord) iter.next();
            assertTrue("should have id " + i + " in " + record, record.getMessage().contains("i" + i + "i"));         
         }

         for (int i = 0; i < stopPoint; i++)
         {
            record = (SimpleRecord) iter.prev();
            int id = stopPoint - i - 1;
            assertTrue(i + " - should have id " + id + " in " + record, record.getMessage().contains("i" + id + "i"));
         }
      }

      //random iteration
      numRec = 200;

      SimpleJournalStore store1 = SimpleJournalStore.createStore("testFreeIteration2", 2014, 20, numRec);

      JournalIterator iter1 = new JournalIterator(store1);

      int index1 = 0;
      int index2 = r.nextInt(numRec);
      boolean forward = true;
      int delta = index2;
      record = null;

      for (int i = 0; i < 5000; i++)
      {
         for (int n = 0; n < delta; n++)
         {
            if (forward)
            {
               record = (SimpleRecord) iter1.next();
               assertNotNull("index1: " + index1 + " index2: " + index2 + " delta: " + delta + " n: " + n, record);
            }
            else
            {
               record = (SimpleRecord) iter1.prev();
               assertNotNull("index1: " + index1 + " index2: " + index2 + " delta: " + delta + " n: " + n, record);
            }
         }

         if (forward)
         {
            int id = index2 - 1;
            assertTrue("should have id " + id + " in record: " + record, record.getMessage().contains("i" + id + "i"));
         }
         else
         {
            int id = index2;
            assertTrue("Should have id " + id + " in record: " + record, record.getMessage().contains("i" + id + "i"));
         }

         index1 = index2;
         index2 = r.nextInt(numRec);
         
         while (index2 == index1)
         {
            index2 = r.nextInt(numRec);
         }

         if (index2 > index1)
         {
            forward = true;
            delta = index2 - index1;
         }
         else
         {
            forward = false;
            delta = index1 - index2;
         }
      }
      
   }

   @Test
   public void testIterationOrder()
   {
      
   }
}
