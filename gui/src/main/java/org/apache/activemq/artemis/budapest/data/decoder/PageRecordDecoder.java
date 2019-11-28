package org.apache.activemq.artemis.budapest.data.decoder;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.activemq.artemis.budapest.data.record.paging.PageDataRecord;
import org.apache.activemq.artemis.budapest.iterator.Bookmark;
import org.apache.activemq.artemis.budapest.iterator.DecodeError;
import org.apache.activemq.artemis.budapest.iterator.JournalFile;
import org.apache.activemq.artemis.budapest.iterator.JournalInputStream;
import org.apache.activemq.artemis.budapest.iterator.JournalRecordDecoder;
import org.apache.activemq.artemis.core.buffers.impl.ChannelBufferWrapper;
import org.apache.activemq.artemis.core.paging.impl.PagedMessageImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.io.IOException;

public class PageRecordDecoder implements JournalRecordDecoder<PageDataRecord>
{
   private static final Logger logger = LoggerFactory.getLogger(PageRecordDecoder.class);

   private static final byte REC_BEGIN = '{';
   private static final byte REC_END = '}';

   private boolean recordStart = false;

   @Override
   public PageDataRecord decode(JournalInputStream stream) throws DecodeError
   {
      //don't use any buffered reader!
      try
      {
         Bookmark position = stream.getCurrentPosition();
         JournalFile currentFile = stream.getCurrentFile();

         if (!recordStart)
         {
            while (stream.readByte() != REC_BEGIN)
            {
            }
            recordStart = true;
            position = stream.getCurrentPosition();
            currentFile = stream.getCurrentFile();
         }
         //not read length
         int len = stream.readInt();

         byte[] data = new byte[len];
         int n = stream.read(data);
         if (n == -1)
         {
            return null;
         }
         if (n != len)
         {
            logger.warn("decoder didn't get enough bytes. Expacted: {} but {}", len, n);
         }
         //decode message. we pass null storage manager or we use NullStorageManager
         PagedMessageImpl msg = new PagedMessageImpl(null);
         ByteBuf buffer = Unpooled.wrappedBuffer(data);
         ChannelBufferWrapper fileBufferWrapper = new ChannelBufferWrapper(buffer);
         fileBufferWrapper.setIndex(0, n);
         msg.decode(fileBufferWrapper);

         //make sure format is correct
         byte last = stream.readByte();
         if (last != REC_END)
         {
            logger.warn("The end char of a record is not right. Currentfile {} position {}", currentFile, position);
         }
         return new PageDataRecord(msg, currentFile, position, data, len);
      }
      catch (EOFException eof)
      {
         return null;
      }
      catch (IOException e)
      {
         throw new DecodeError(e.getMessage());
      }
   }

}
