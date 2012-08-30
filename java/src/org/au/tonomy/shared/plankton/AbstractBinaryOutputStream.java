package org.au.tonomy.shared.plankton;


/**
 * A simple byte[] backed binary output stream.
 */
public abstract class AbstractBinaryOutputStream implements IBinaryOutputStream {

  @Override
  public IBinaryOutputStream addByte(int value) {
    ensureCapacityDelta(1);
    append(value);
    return this;
  }

  @Override
  public IBinaryOutputStream addInt32(int value) {
    ensureCapacityDelta(8);
    while ((value & ~0x7F) != 0) {
      append((value & 0x7F) | 0x80);
      value = value >>> 7;
    }
    append(value);
    return this;
  }

  @Override
  public IBinaryOutputStream addBlob(byte[] data) {
    addInt32(data.length);
    ensureCapacityDelta(data.length);
    for (int i = 0; i < data.length; i++)
      append(data[i]);
    return this;
  }

  protected abstract void ensureCapacityDelta(int delta);

  protected abstract void append(int value);

}
