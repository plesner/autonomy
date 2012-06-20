package org.au.tonomy.client.webgl;

import org.au.tonomy.shared.util.ExtraMath;
import org.au.tonomy.shared.util.MatrixArray;

/**
 * A 4x4 matrix backed by a Float32Array.
 */
public class Mat4 extends Float32Array implements MatrixArray {

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
  public final Mat4 resetToIdentity() {
    ExtraMath.resetMat4Identity(this);
    return this;
  }

  /**
   * Translates this matrix by the given vector.
   */
  public final Mat4 translate(double x, double y, double z) {
    ExtraMath.translateMat4(this, x, y, z);
    return this;
  }

  /**
   * Generates a frustum matrix with the given bounds
   */
  public final Mat4 resetFrustum(double left, double right, double bottom,
      double top, double near, double far) {
    ExtraMath.resetMat4Frustum(this, left, right, bottom, top, near, far);
    return this;
  };

  /**
   * Generates a perspective projection matrix with the given bounds
   */
  public final Mat4 resetPerspective(double fovy, double aspect, double near,
      double far) {
    ExtraMath.resetMat4Perspective(this, fovy, aspect, near, far);
    return this;
  };

}
