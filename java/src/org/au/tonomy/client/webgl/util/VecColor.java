package org.au.tonomy.client.webgl.util;


public class VecColor extends Vec4 {

  protected VecColor() { }

  /**
   * Creates a new color object.
   */
  public static native VecColor create(double r, double g, double b, double alpha) /*-{
    return new Float32Array([r, g, b, alpha]);
  }-*/;

  public static final VecColor BLACK = VecColor.create(0.0, 0.0, 0.0, 1.0);
  public static final VecColor GRAY = VecColor.create(0.5, 0.5, 0.5, 1.0);

}
