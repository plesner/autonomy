package org.au.tonomy.shared.util;


/**
 * Assertion library to avoid using Java's which is difficult to
 * control.
 */
public class Assert {

  public static void that(boolean value) {
    if (!value) {
      throw new AssertionError();
    }
  }

  public static <T> T notNull(T obj) {
    that(obj != null);
    return obj;
  }

  public static <T> T isNull(T obj) {
    that(obj == null);
    return obj;
  }

}
