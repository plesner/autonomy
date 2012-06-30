package org.au.tonomy.client.presentation;

import org.au.tonomy.shared.util.IRect;
/**
 * Simple implementation of a mutable rectangle.
 */
public class MutableRect implements IRect {

  private double left;
  private double right;
  private double bottom;
  private double top;

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

  /**
   * Moves this rectangle by a given delta, maintaining the same width
   * and height.
   */
  public void translate(double dx, double dy) {
    this.left += dx;
    this.right += dx;
    this.top += dy;
    this.bottom += dy;
  }

  public void reset(double left, double right, double bottom, double top) {
    this.left = left;
    this.right = right;
    this.bottom = bottom;
    this.top = top;
  }

}
