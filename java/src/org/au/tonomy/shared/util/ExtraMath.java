package org.au.tonomy.shared.util;



/**
 * Extra math stuff.
 */
public class ExtraMath {

  /**
   * Bob Palais' TAU = 2 PI.
   */
  public static final double TAU = 2 * Math.PI;

  /**
   * Multiply by this to convert from degrees to radians.
   */
  public static final double DEGREES_TO_RADIANS = TAU / 360;

  /**
   * Resets a 4x4 matrix to the identity.
   */
  public static void resetMat4Identity(MatrixArray mat) {
    assert mat.getLength() == 16;
    for (int i = 0; i < 4; i++) {
      for (int j = 0; j < 4; j++)
        mat.set(i + 4 * j, i == j ? 1 : 0);
    }
  }

  /**
   * Translate this matrix by the given vector.
   */
  public static void translateMat4(MatrixArray mat, double x, double y, double z) {
    assert mat.getLength() == 16;
    mat.set(12, x * mat.get(0) + y * mat.get(4) + z * mat.get(8) + mat.get(12));
    mat.set(13, x * mat.get(1) + y * mat.get(5) + z * mat.get(9) + mat.get(13));
    mat.set(14, x * mat.get(2) + y * mat.get(6) + z * mat.get(10) + mat.get(14));
    mat.set(15, x * mat.get(3) + y * mat.get(7) + z * mat.get(11) + mat.get(15));
  }

  /**
   * Generates a frustum matrix with the given bounds
   */
  public static void resetMat4Frustum(MatrixArray mat, double left, double right,
      double bottom, double top, double near, double far) {
    assert mat.getLength() == 16;
    double rl = (right - left);
    double tb = (top - bottom);
    double fn = (far - near);
    mat.set(0, (near * 2) / rl);
    mat.set(1, 0);
    mat.set(2, 0);
    mat.set(3, 0);
    mat.set(4, 0);
    mat.set(5, (near * 2) / tb);
    mat.set(6, 0);
    mat.set(7, 0);
    mat.set(8, (right + left) / rl);
    mat.set(9, (top + bottom) / tb);
    mat.set(10, -(far + near) / fn);
    mat.set(11, -1);
    mat.set(12, 0);
    mat.set(13, 0);
    mat.set(14, -(far * near * 2) / fn);
    mat.set(15, 0);
  }

  /**
   * Generates a perspective projection matrix with the given bounds
   */
  public static void resetMat4Perspective(MatrixArray mat, double fovy, double aspect,
      double near, double far) {
    double top = near * Math.tan(fovy * DEGREES_TO_RADIANS / 2);
    double right = top * aspect;
    resetMat4Frustum(mat, -right, right, -top, top, near, far);
  };

}
