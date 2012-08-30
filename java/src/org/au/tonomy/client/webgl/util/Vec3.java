package org.au.tonomy.client.webgl.util;

import org.au.tonomy.client.browser.Float32Array;

public class Vec3 extends Float32Array {

  protected Vec3() { }

  /**
   * Creates a new empty 3-vector.
   */
  public static native Vec3 create() /*-{
    return new Float32Array(3);
  }-*/;

  /**
   * Creates a new empty 3-vector.
   */
  public static native Vec3 create(double x, double y, double z) /*-{
    return new Float32Array([x, y, z]);
  }-*/;

}
