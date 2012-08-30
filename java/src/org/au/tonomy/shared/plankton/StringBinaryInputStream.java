package org.au.tonomy.shared.plankton;


/**
 * Reads a byte array as binary input.
 */
public class StringBinaryInputStream implements IBinaryInputStream {

  private int cursor = 0;
  private final String data;

  public StringBinaryInputStream(String data) {
    this.data = data;
  }

  @Override
  public int nextByte() throws DecodingError {
    if (cursor < data.length()) {
      return data.charAt(cursor++);
    } else {
      throw new DecodingError("Unexpected end of data reading byte");
    }
  }

  @Override
  public int nextInt32() throws DecodingError {
    int result = 0;
    int offset = 0;
    int current;
    do {
      current = nextByte();
      result |= (current & 0x7F) << offset;
      offset += 7;
    } while ((current & 0x80) != 0);
    return result;
  }

  private static final byte[] EMPTY_BYTES = new byte[0];
  @Override
  public byte[] nextBlob() throws DecodingError {
    int bytes = nextInt32();
    if (bytes == 0)
      return EMPTY_BYTES;
    if (cursor + bytes > data.length())
      throw new DecodingError("Unexpected end of data reading blob");
    byte[] result = new byte[bytes];
    for (int i = 0; i < bytes; i++)
      result[i] = (byte) data.charAt(cursor++);
    return result;
  }

}
