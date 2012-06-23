package org.au.tonomy.client.webgl.util;

import org.au.tonomy.client.webgl.Float32Array;

/**
 * A wrapper around the gl-matrix library.
 */
public class Mat4 extends Float32Array {

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

  public final native Mat4 moveAndScale2D(double x, double y, double scale) /*-{
    $wnd.mat4.identity(this);
    $wnd.mat4.translate(this, [x, y, 0]);
    $wnd.mat4.scale(this, [scale, scale, 1]);
    return this;
  }-*/;

  /**
   * Resets this matrix to a perspective projection with the given bounds.
   */
  public final native Mat4 resetPerspective(double fieldOfView, double aspectRatio,
      double nearBoundary, double farBoundary) /*-{
    return $wnd.mat4.perspective(fieldOfView, aspectRatio, nearBoundary,
        farBoundary, this);
  }-*/;

}
