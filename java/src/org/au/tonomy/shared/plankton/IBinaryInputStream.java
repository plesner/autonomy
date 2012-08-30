package org.au.tonomy.shared.plankton;
/**
 * A stream of bytes that can be read.
 */
public interface IBinaryInputStream {

  /**
   * Returns the next single byte.
   */
  public int nextByte() throws DecodingError;

  /**
   * Decodes and returns the next int32.
   */
  public int nextInt32() throws DecodingError;

  /**
   * Returns the next binary blob.
   */
  public byte[] nextBlob() throws DecodingError;

}
