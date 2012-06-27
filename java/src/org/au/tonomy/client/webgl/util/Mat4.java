package org.au.tonomy.client.webgl.util;

import org.au.tonomy.client.webgl.Float32Array;
import org.au.tonomy.shared.util.IMatrix;

/**
 * A wrapper around the gl-matrix library.
 */
public class Mat4 extends Float32Array implements IMatrix<Vec4> {

  protected Mat4() { }

  /**
   * Creates a new empty 4x4 matrix.
   */
  public static native Mat4 create() /*-{
    return $wnd.mat4.create();
  }-*/;

  /**
   * Resets this to the identity matrix.
   */
  public final native Mat4 resetToIdentity() /*-{
    return $wnd.mat4.identity(this);
  }-*/;

  /**
   * Translates this matrix by the given vector.
   */
  public final native Mat4 translate(double x, double y, double z) /*-{
    return $wnd.mat4.translate(this, [x, y, z]);
  }-*/;

  /**
   * Scales this matrix by the given vector.
   */
  public final native Mat4 scale(double sx, double sy, double sz) /*-{
    return $wnd.mat4.scale(this, [sx, sy, sz]);
  }-*/;

  /**
   * Resets this matrix to a frustum with the given bounds
   */
  public final native Mat4 resetFrustum(double left, double right,
      double bottom, double top, double near, double far) /*-{
    return $wnd.mat4.frustum(left, right, bottom, top, near, far, this);
  }-*/;

  /**
   * Returns a new matrix that is the inverse of this one.
   */
  public final native Mat4 inverse() /*-{
    return $wnd.mat4.inverse(this, $wnd.mat4.create());
  }-*/;

  /**
   * Returns a new vector that is the result of multiplying this matrix
   * with the given vector.
   */
  public final native Vec4 multiply(Vec4 vec) /*-{
    return $wnd.mat4.multiplyVec4(this, vec, $wnd.vec4.create());
  }-*/;

  @Override
  public final Vec4 multiply(double x, double y, double z, double w) {
    return multiply(Vec4.create(x, y, z, w));
  }

  /**
   * Resets this matrix to a perspective projection with the given bounds.
   */
  public final native Mat4 resetPerspective(double fieldOfView, double aspectRatio,
      double nearBoundary, double farBoundary) /*-{
    return $wnd.mat4.perspective(fieldOfView, aspectRatio, nearBoundary,
        farBoundary, this);
  }-*/;

  public final native Mat4 resetOrtho(double left, double right, double bottom,
      double top, double near, double far) /*-{
    return $wnd.mat4.ortho(left, right, bottom, top, near, far, this);
  }-*/;

  public final String asString() {
    return "[" +
      "[" + get( 0) + ", " + get( 1) + ", " + get( 2) + ", " + get( 3) + "]" +
      "[" + get( 4) + ", " + get( 5) + ", " + get( 6) + ", " + get( 7) + "]" +
      "[" + get( 8) + ", " + get( 9) + ", " + get(10) + ", " + get(11) + "]" +
      "[" + get(12) + ", " + get(13) + ", " + get(14) + ", " + get(15) + "]" +
    "]";
  }

}
