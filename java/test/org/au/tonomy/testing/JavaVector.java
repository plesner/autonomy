package org.au.tonomy.testing;

import org.au.tonomy.shared.util.Assert;
import org.au.tonomy.shared.util.IVector;

public class JavaVector implements IVector {

  private final double[] values;

  private JavaVector(double[] values) {
    Assert.equals(4, values.length);
    this.values = values;
  }

  public static JavaVector create(double... args) {
    return new JavaVector(args);
  }

  @Override
  public double get(int index) {
    return values[index];
  }

  @Override
  public double getX() {
    return get(0);
  }

  @Override
  public double getY() {
    return get(1);
  }

  @Override
  public double getZ() {
    return get(2);
  }

}
