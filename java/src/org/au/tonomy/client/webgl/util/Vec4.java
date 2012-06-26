package org.au.tonomy.client.webgl.util;

import org.au.tonomy.client.webgl.Float32Array;

public class Vec4 extends Float32Array {

  protected Vec4() { }

  /**
   * Creates a new empty 4-vector.
   */
  public static native Vec4 create() /*-{
    return $wnd.vec4.create();
  }-*/;

  /**
   * Creates a new 4-vector with the given components.
   */
  public static native Vec4 create(double x, double y, double z, double w) /*-{
    return new Float32Array([x, y, z, w]);
  }-*/;

  public final native Vec4 applyScale(double scale) /*-{
    return $wnd.vec4.scale(this, scale);
  }-*/;

  public final String asString() {
    return "[" + get(0) + ", " + get(1) + ", " + get(2) + ", " + get(3) + "]";
  }

}
