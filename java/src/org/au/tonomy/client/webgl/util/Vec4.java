package org.au.tonomy.client.webgl.util;

import org.au.tonomy.client.webgl.Float32Array;

public class Vec4 extends Float32Array {

  protected Vec4() { }

  /**
   * Creates a new empty 4-vector.
   */
  public static native Vec4 create() /*-{
    return new Float32Array(4);
  }-*/;

}
