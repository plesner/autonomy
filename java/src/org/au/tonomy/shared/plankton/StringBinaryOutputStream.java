package org.au.tonomy.shared.plankton;


public class StringBinaryOutputStream extends AbstractBinaryOutputStream {

  private int cursor = 0;
  private char[] buffer = new char[16];

  @Override
  protected void ensureCapacityDelta(int delta) {
    int newSize = cursor + delta;
    if (newSize >= buffer.length) {
      char[] newBuffer = new char[newSize * 2];
      System.arraycopy(buffer, 0, newBuffer, 0, cursor);
      this.buffer = newBuffer;
    }
  }

  @Override
  protected void append(int value) {
    buffer[cursor++] = (char) (value & 0xFF);
  }

  public String flush() {
    return new String(buffer, 0, cursor);
  }

}
