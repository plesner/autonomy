package org.au.tonomy.client.webgl.util;

public class Color extends Vec4 {

  protected Color() { }

  /**
   * Creates a new color object.
   */
  public static native Color create(double r, double g, double b, double alpha) /*-{
    return new Float32Array([r, g, b, alpha]);
  }-*/;

  public static final Color BLACK = Color.create(0.0, 0.0, 0.0, 1.0);
  public static final Color GRAY = Color.create(0.5, 0.5, 0.5, 1.0);

}
