package org.au.tonomy.shared.plankton;
/**
 * A stream of bytes emitted.
 */
public interface IBinaryOutputStream {

  /**
   * Write a single byte.
   */
  public IBinaryOutputStream addByte(int value);

  /**
   * Encode an int32 using a varint encoding.
   */
  public IBinaryOutputStream addInt32(int value);

  /**
   * Write a complete binary blob.
   */
  public IBinaryOutputStream addBlob(byte[] data);

}
