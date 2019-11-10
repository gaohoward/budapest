package org.apache.activemq.artemis.budapest.data.decoder;

import org.apache.activemq.artemis.budapest.data.record.DataRecord;
import org.apache.activemq.artemis.budapest.data.store.LogFile;
import org.apache.activemq.artemis.budapest.iterator.Bookmark;
import org.apache.activemq.artemis.budapest.iterator.DecodeError;
import org.apache.activemq.artemis.budapest.iterator.JournalInputStream;
import org.apache.activemq.artemis.budapest.iterator.JournalRecordDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Note: for DOS style log, the [CR][LF] marks the end of line.
 * The decoder returns a line as soon as it has read a [CR] or [LF].
 * If it is a CR, LF will only be ignored on next read.
 * That poses a problem with the iterator, however, that when
 * the iterator remembers a bookmark who happens to point to
 * the end of a [CR][LF] line, the position is at [LF]. This won't
 * cause any problem for a continuous decoding, where the decoder
 * remembers the state and ignores the leading [LF] when decoding
 * next line. But if the iterator is reset with the bookmark,
 * it has no context of this leading [LF] and decode it as an extra empty
 * line.
 */
public class LogRecordDecoder implements JournalRecordDecoder<DataRecord>
{
   private static final Logger logger = LoggerFactory.getLogger(LogRecordDecoder.class);

   private boolean ignoreLF;
   private boolean eof;
   private ByteArrayOutputStream sb = new ByteArrayOutputStream(512);
   private byte[] cb = new byte[1];

   public LogRecordDecoder()
   {
   }

   public DataRecord decode(JournalInputStream stream) throws DecodeError
   {
      //don't use any buffered reader!
      try
      {
         Bookmark position = stream.getCurrentPosition();

         if (position.isBegin()) {
            //some malformated file (like contains single CR(0D) char) can disrupt the flag
            //so reset it as best effort.
            ignoreLF = false;
         }

         sb.reset();
         boolean stop = false;

         while (!stop)
         {
            int n = stream.read(cb, 0, 1);

            if (n == -1)
            {
               logger.debug("reach the bottom of a file");
               eof = true;
               break;
            }
            else if (cb[0] == '\r')
            {
               logger.debug("read a CR , now eof? {}", eof);
               ignoreLF = true;
               break;
            }
            else if (cb[0] == '\n')
            {
               logger.debug("read a LF, ignoreLF? {}", ignoreLF);
               if (ignoreLF)
               {
                  ignoreLF = false;
                  continue;
               }
               //a new line
               break;
            }
            else
            {
               sb.write(cb, 0, 1);
            }
         }
         if (eof && sb.size() == 0)
         {
            logger.debug("decoder returning null because eof and size 0");
            eof = false;
            ignoreLF = false;
            return null;
         }
         //to do : support different encoding.
         DataRecord record = new DataRecord(sb.toString(), (LogFile)stream.getCurrentFile(), position);
         return record;
      }
      catch (IOException e)
      {
         logger.error("decoding error", e);
         throw new DecodeError(e.getMessage());
      }
      finally
      {
         if (eof)
         {
            eof = false;
         }
      }
   }

   public void initFlag(byte b1, byte b2)
   {
      if (b1 == '\r' && b2 == '\n')
      {
         ignoreLF = true;
      }
   }

}
