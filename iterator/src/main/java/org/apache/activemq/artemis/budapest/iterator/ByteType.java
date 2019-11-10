package org.apache.activemq.artemis.budapest.iterator;

/**
 * Defines the attributes of a byte in a journal 
 * when decoding a record.
 * 
 * INVALID: the byte is not a valid byte, the store 
 * must be corrupted.
 * 
 * PADDING: any byte in between two records, usu
 * meaningless except for alignment
 * 
 * UNUSED: free space. For an appending only journal
 * this would mean the end of all records.
 * 
 * @author howard
 *
 */
public enum ByteType
{
   INVALID,
   PADDING,
   UNUSED
}
