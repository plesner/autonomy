package org.au.tonomy.shared.util;
/**
 * Container class for miscellaneous functions.
 */
public class Misc {

  /**
   * Appends the lowest 'digits' hex digits of the given value to the
   * given buffer.
   */
  public static void writeHexDigits(int value, int digits, StringBuilder buf) {
    for (int i = digits - 1; i >= 0; i--) {
      int nibble = (value >> (i << 2)) & 0xF;
      buf.append("0123456789abcdef".charAt(nibble));
    }
  }

}
