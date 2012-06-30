package org.au.tonomy.shared.util;
/**
 * Abstract rectangle interface.
 */
public interface IRect {

  /**
   * Returns the left bound.
   */
  public double getLeft();

  /**
   * Returns the right bound.
   */
  public double getRight();

  /**
   * Returns the top bound.
   */
  public double getTop();

  /**
   * Returns the bottom bound.
   */
  public double getBottom();

  /**
   * Returns the distance between left and right.
   */
  public double getWidth();

  /**
   * Returns the distance between top and bottom.
   */
  public double getHeight();

}
