package org.jboss.budapest.test;

import org.jboss.budapest.data.iterator.DataRecordIterator;
import org.jboss.budapest.data.journal.JournalDataStore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class JournalIteratorTest {

   @Test
   public void testIterator() throws Exception {

      File journalDir = new File("src/test/resources/data1");
      JournalDataStore store = new JournalDataStore(journalDir);
      DataRecordIterator iter = new DataRecordIterator(store);

      while (iter.hasNext()) {
         System.out.println("record: " + iter.next());
      }

   }
}
