package org.apache.activemq.artemis.budapest.iterator;

public interface JournalRecordDecoder<T>
{

   /**
    * decode a record from stream. 
    * @param stream
    * @return The decoded record, or null if end of journal
    * (Not necessarily the end of stream) is reached
    * 
    * @throws DecodeError
    */
   T decode(JournalInputStream stream) throws DecodeError;

}
