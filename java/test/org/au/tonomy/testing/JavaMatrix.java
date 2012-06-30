package org.au.tonomy.testing;

import org.au.tonomy.shared.util.Assert;
import org.au.tonomy.shared.util.IMatrix;

public class JavaMatrix implements IMatrix<JavaVector> {

  private final double[] entries;

  private JavaMatrix(double[] entries) {
    Assert.that(entries.length == 16);
    this.entries = entries;
  }

  @Override
  public JavaVector multiply(double x, double y, double z, double w) {
    double rX = x * entries[ 0] + y * entries[ 1] + z * entries[ 2] + w * entries[ 3];
    double rY = x * entries[ 4] + y * entries[ 5] + z * entries[ 6] + w * entries[ 7];
    double rZ = x * entries[ 8] + y * entries[ 9] + z * entries[10] + w * entries[11];
    double rW = x * entries[12] + y * entries[13] + z * entries[14] + w * entries[15];
    return JavaVector.create(rX, rY, rZ, rW);
  }

}
