package org.apache.activemq.artemis.budapest.iterator;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Read a byte from JournalStore, including contents in all journal files
 * within, in order, and excluding header bytes.
 * The stream is used for a decoder to decode all records in the store.
 * The Decoder only knows the bytes that constitutes a record. It may read
 * from this stream, in between records, throughout the whole stream,
 * a byte that it doesn't recognize. In such cases, the decoder need work
 * with the stream to find out the nature of the byte and how to process it
 * , which may be:
 * 
 * 1. A padding byte belonging to a record, which the decoder should ignore and read next.
 * 2. A byte from unused space, usually at the last part of the store,
 *    which the decoder should ignore and stops decoding.
 * 3. An invalid byte, which means the journal store is corrupted, for which the decoder
 *    should throw an exception.
 *    
 * See method verifyUnknownByte().    
 */
public abstract class JournalInputStream extends DataInputStream {

   public JournalInputStream(InputStream in)
   {
      super(in);
   }

   /**
    * Tells whether the byte just read is a padding byte, unused byte, or
    * invalid byte.
    * 
    * this method must be called by a decoder when it can't decode it (in
    * between records, which means after a record has just been decoded
    * but before a new record is begin)
    * Note the decoder shouldn't call it if it knows the byte is a bad one
    * within a record.
    * 
    * @param theByte
    * @return true if the byte is a padding or unused. False otherwise.
    */
   public abstract ByteType verifyUnknownByte(int theByte);

   public abstract Bookmark getCurrentPosition() throws IOException;

   public abstract JournalFile getCurrentFile();
}
