package org.au.tonomy.client.webgl.util;

import java.util.EnumMap;
/**
 * A wrapper around a color vector which allows the color to be adjusted
 * lighter and darker.
 */
public class Color {

  /**
   * Types of color adjustment.
   */
  public enum Adjustment {

    LIGHTER(false, 0.75),
    DARKER(true, 0.75);

    private final boolean adjustDown;
    private final double amount;

    private Adjustment(boolean adjustDown, double amount) {
      this.adjustDown = adjustDown;
      this.amount = amount;
    }

    /**
     * Returns a new color that is the given color adjusted appropriately.
     */
    private Color adjust(Color input) {
      VecColor vector = input.getVector();
      double r = adjustComponent(vector.get(0));
      double g = adjustComponent(vector.get(1));
      double b = adjustComponent(vector.get(2));
      double alpha = vector.get(3);
      return new Color(VecColor.create(r, g, b, alpha));
    }

    private double adjustComponent(double value) {
      return adjustDown
        ? value * amount
        : 1 - ((1 - value) * amount);
    }

  }

  private final VecColor value;
  private EnumMap<Adjustment, Color> adjustedCache;

  private Color(VecColor value) {
    this.value = value;
  }

  /**
   * Creates a new color object with the given components.
   */
  public static Color create(double r, double g, double b, double alpha) {
    return new Color(VecColor.create(r, g, b, alpha));
  }

  /**
   * Returns the underlying color vector.
   */
  public VecColor getVector() {
    return value;
  }

  /**
   * Returns a color that is similar to this one but adjusted in the
   * specified direction. Calling this multiple times will yield the
   * same result object.
   */
  public Color adjust(Adjustment change) {
    EnumMap<Adjustment, Color> cache = ensureAdjustedCache();
    if (!cache.containsKey(change))
      cache.put(change, change.adjust(this));
    return cache.get(change);
  }

  private EnumMap<Adjustment, Color> ensureAdjustedCache() {
    if (adjustedCache == null)
      adjustedCache = new EnumMap<Adjustment, Color>(Adjustment.class);
    return adjustedCache;
  }

  public static final Color BLACK = new Color(VecColor.BLACK);

}
