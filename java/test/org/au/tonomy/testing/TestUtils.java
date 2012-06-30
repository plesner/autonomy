package org.au.tonomy.testing;

import junit.framework.Assert;

import org.au.tonomy.shared.world.Hex;

public class TestUtils extends Assert {

  /**
   * A very small quantity.
   */
  public static final double EPSILON = 1e-10;

  /**
   * A convenient abbreviation for the inner radius.
   */
  public static final double A = Hex.INNER_RADIUS;

  /**
   * A convenient abbreviation for the inner diameter.
   */
  public static final double D = Hex.INNER_DIAMETER;

  /**
   * Assert that the value found is within epsilon of the expected
   * value.
   */
  public static void assertClose(double expected, double found) {
    assertEquals(expected, found, EPSILON);
  }

  /**
   * Create a pair as a 2-element array of doubles.
   */
  public static double[] pair(double a, double b) {
    return new double[] {a, b};
  }

  /**
   * Create a pair as a 2-element array of ints.
   */
  public static int[] pair(int a, int b) {
    return new int[] {a, b};
  }

  /**
   * Assert that the given pair of doubles has the expected entry values.
   */
  public static void assertClose(double x, double y, double[] found) {
    assertEquals(2, found.length);
    assertClose(x, found[0]);
    assertClose(y, found[1]);
  }

}
