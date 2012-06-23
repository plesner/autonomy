package org.au.tonomy.client.webgl.util;

import org.au.tonomy.client.webgl.Float32Array;
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
   * Scales this matrix by the given vector.
   */
  public final native Mat4 scale(double sx, double sy, double sz) /*-{
    this[ 0] *= sx; this[ 1] *= sx; this[ 2] *= sx; this[ 3] *= sx;
    this[ 4] *= sy; this[ 5] *= sy; this[ 6] *= sy; this[ 7] *= sy;
    this[ 8] *= sz; this[ 9] *= sz; this[10] *= sz; this[11] *= sz;
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

  /**
   * Returns the inverse of this matrix. Leaves this matrix unchanged.
   */
  public final native Mat4 inverse() /*-{
    var a00 = this[0], a01 = this[1], a02 = this[2], a03 = this[3],
        a10 = this[4], a11 = this[5], a12 = this[6], a13 = this[7],
        a20 = this[8], a21 = this[9], a22 = this[10], a23 = this[11],
        a30 = this[12], a31 = this[13], a32 = this[14], a33 = this[15];

    var b00 = a00 * a11 - a01 * a10,
        b01 = a00 * a12 - a02 * a10,
        b02 = a00 * a13 - a03 * a10,
        b03 = a01 * a12 - a02 * a11,
        b04 = a01 * a13 - a03 * a11,
        b05 = a02 * a13 - a03 * a12,
        b06 = a20 * a31 - a21 * a30,
        b07 = a20 * a32 - a22 * a30,
        b08 = a20 * a33 - a23 * a30,
        b09 = a21 * a32 - a22 * a31,
        b10 = a21 * a33 - a23 * a31,
        b11 = a22 * a33 - a23 * a32;

    var d = (b00 * b11 - b01 * b10 + b02 * b09 + b03 * b08 - b04 * b07 + b05 * b06);
    if (!d)
      return null;

    var invDet = 1 / d;
    var dest = new Float32Array(16);
    dest[0] = (a11 * b11 - a12 * b10 + a13 * b09) * invDet;
    dest[1] = (-a01 * b11 + a02 * b10 - a03 * b09) * invDet;
    dest[2] = (a31 * b05 - a32 * b04 + a33 * b03) * invDet;
    dest[3] = (-a21 * b05 + a22 * b04 - a23 * b03) * invDet;
    dest[4] = (-a10 * b11 + a12 * b08 - a13 * b07) * invDet;
    dest[5] = (a00 * b11 - a02 * b08 + a03 * b07) * invDet;
    dest[6] = (-a30 * b05 + a32 * b02 - a33 * b01) * invDet;
    dest[7] = (a20 * b05 - a22 * b02 + a23 * b01) * invDet;
    dest[8] = (a10 * b10 - a11 * b08 + a13 * b06) * invDet;
    dest[9] = (-a00 * b10 + a01 * b08 - a03 * b06) * invDet;
    dest[10] = (a30 * b04 - a31 * b02 + a33 * b00) * invDet;
    dest[11] = (-a20 * b04 + a21 * b02 - a23 * b00) * invDet;
    dest[12] = (-a10 * b09 + a11 * b07 - a12 * b06) * invDet;
    dest[13] = (a00 * b09 - a01 * b07 + a02 * b06) * invDet;
    dest[14] = (-a30 * b03 + a31 * b01 - a32 * b00) * invDet;
    dest[15] = (a20 * b03 - a21 * b01 + a22 * b00) * invDet;

    return dest;
  }-*/;

  public final native Vec3 multiply(double x, double y, double z) /*-{
    var result = new Float32Array(3);
    result[0] = this[ 0] * x + this[ 4] * y + this[ 8] * z + this[12];
    result[1] = this[ 1] * x + this[ 5] * y + this[ 9] * z + this[13];
    result[2] = this[ 2] * x + this[ 6] * y + this[10] * z + this[14];
    return result;
  }-*/;

}
