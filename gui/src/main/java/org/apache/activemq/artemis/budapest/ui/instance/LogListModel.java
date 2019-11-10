package org.apache.activemq.artemis.budapest.ui.instance;

import org.apache.activemq.artemis.budapest.data.record.DataRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.AbstractListModel;
import java.util.ArrayList;

//a list model to replace DefaultListModel, which is
//slow.
public class LogListModel extends AbstractListModel<DataRecord>
{
   private static final long serialVersionUID = 3026665434679058360L;

   private static final Logger logger = LoggerFactory.getLogger(LogListModel.class);

   protected final int maxLen;
   private DataRecord[] elements;
   private int size;

   public LogListModel(int maxLen)
   {
      this.maxLen = maxLen;
      elements = new DataRecord[maxLen];
      size = 0;
   }

   public DataRecord getElementAt(int index)
   {
      if (index >= size)
      {
         throw new ArrayIndexOutOfBoundsException();
      }

      return elements[index];
   }

   public void addPage(ArrayList<DataRecord> records)
   {
      if (records.size() > maxLen)
      {
         logger.error("Records too many for model {} > {}", records.size(), maxLen);
         throw new ArrayIndexOutOfBoundsException();
      }
      int lastPageSize = size;
      int newSize = records.size();
      size = 0;

      for (int i = 0; i < newSize; i++)
      {
         internalAddElement(records.get(i));
      }

      if (lastPageSize >= newSize)
      {
         //some changed some removed.
         fireContentsChanged(this, 0, size - 1);
         for (int i = size; i < lastPageSize; i++)
         {
            elements[i] = null;
         }
         fireIntervalRemoved(this, size, lastPageSize - 1);
      }
      else
      {
         //some changed some added
         fireContentsChanged(this, 0, lastPageSize - 1);
         fireIntervalAdded(this, lastPageSize, size - 1);
      }
   }

   private void internalAddElement(DataRecord record)
   {
      elements[size] = record;
      size++;
   }

   public int getPageSize()
   {
      return maxLen;
   }

   public int getSize()
   {
      return size;
   }
}
