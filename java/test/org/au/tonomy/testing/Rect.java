package org.au.tonomy.testing;

import org.au.tonomy.shared.util.IRect;

public class Rect implements IRect {

  private final double left;
  private final double right;
  private final double bottom;
  private final double top;

  public Rect(double left, double right, double bottom, double top) {
    this.left = left;
    this.right = right;
    this.bottom = bottom;
    this.top = top;
  }

  @Override
  public double getLeft() {
    return left;
  }

  @Override
  public double getRight() {
    return right;
  }

  @Override
  public double getTop() {
    return top;
  }

  @Override
  public double getBottom() {
    return bottom;
  }

  @Override
  public double getWidth() {
    return right - left;
  }

  @Override
  public double getHeight() {
    return top - bottom;
  }

}
