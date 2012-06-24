package org.au.tonomy.shared.world;
/**
 * A viewport defines the normalized cartesian coordinates of a section
 * of a hex grid.
 */
public class Viewport {

  private double left;
  private double right;
  private double top;
  private double bottom;

  public Viewport(double left, double bottom, double right, double top) {
    this.left = left;
    this.right = right;
    this.top = top;
    this.bottom = bottom;
  }

  public double getLeft() {
    return this.left;
  }

  public double getRight() {
    return this.right;
  }

  public double getTop() {
    return this.top;
  }

  public double getBottom() {
    return this.bottom;
  }

  public double getWidth() {
    return right - left;
  }

  public double getHeight() {
    return top - bottom;
  }

  public void move(double x, double y) {
    this.left += x;
    this.right += x;
    this.bottom += y;
    this.top += y;
  }

  @Override
  public String toString() {
    return "(" + left + ", " + bottom + ")->(" + right + ", " + top + ")";
  }

}
