package org.au.tonomy.shared.util;



/**
 * Assertion library to avoid using Java's which is difficult to
 * control.
 */
public class Assert {

  /**
   * Set this to false to disable the most expensive assertions.
   */
  public static final boolean enableExpensiveAssertions = true;

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

  public static void equals(Object a, Object b) {
    that(a == null ? (b == null) : (a.equals(b)));
  }

}
