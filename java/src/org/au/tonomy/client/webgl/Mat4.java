package org.au.tonomy.client.webgl;

import org.au.tonomy.shared.util.ExtraMath;

/**
 * A 4x4 matrix backed by a Float32Array.
 */
public class Mat4 extends Float32Array {

  protected Mat4() { }

  /**
   * Creates a new empty 4x4 matrix.
   */
  public static native Mat4 create() /*-{
    return new Float32Array(16);
  }-*/;

  /**
   * Resets this to the identity matrix.
   */
  public final native Mat4 resetToIdentity() /*-{
    this[ 0] = 1; this[ 1] = 0; this[ 2] = 0; this[ 3] = 0;
    this[ 4] = 0; this[ 5] = 1; this[ 6] = 0; this[ 7] = 0;
    this[ 8] = 0; this[ 9] = 0; this[10] = 1; this[11] = 0;
    this[12] = 0; this[13] = 0; this[14] = 0; this[15] = 1;
    return this;
  }-*/;

  /**
   * Translates this matrix by the given vector.
   */
  public final native Mat4 translate(double x, double y, double z) /*-{
    this[12] = x * this[ 0] + y * this[ 4] + z * this[ 8] + this[12];
    this[13] = x * this[ 1] + y * this[ 5] + z * this[ 9] + this[13];
    this[14] = x * this[ 2] + y * this[ 6] + z * this[10] + this[14];
    this[15] = x * this[ 3] + y * this[ 7] + z * this[11] + this[15];
    return this;
  }-*/;

  /**
   * Resets this matrix to a frustum with the given bounds
   */
  public final native Mat4 resetFrustum(double l, double r,
      double b, double t, double n, double f) /*-{
    var rl = (r - l);
    var tb = (t - b);
    var fn = (f - n);
    this[ 0] = (n*2)/rl; this[ 1] = 0;        this[ 2] = 0;           this[ 3] = 0;
    this[ 4] = 0;        this[ 5] = (n*2)/tb; this[ 6] = 0;           this[ 7] = 0;
    this[ 8] = (r+l)/rl; this[ 9] = (t+b)/tb; this[10] = -(f+n)/fn;   this[11] = -1;
    this[12] = 0;        this[13] = 0;        this[14] = -(f*n*2)/fn; this[15] = 0;
    return this;
  }-*/;

  /**
   * Resets this matrix to a perspective projection with the given bounds.
   */
  public final Mat4 resetPerspective(double fieldOfView, double aspectRatio,
      double nearBoundary, double farBoundary) {
    double top = nearBoundary * Math.tan(fieldOfView * ExtraMath.DEGREES_TO_RADIANS / 2);
    double right = top * aspectRatio;
    return this.resetFrustum(-right, right, -top, top, nearBoundary, farBoundary);
  };

}
