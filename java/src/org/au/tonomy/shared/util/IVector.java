package org.au.tonomy.shared.util;
/**
 * Abstract vector interface.
 */
public interface IVector {

  /**
   * Returns the index'th component of this vector.
   */
  public double get(int index);

  /**
   * Returns the 0th component.
   */
  public double getX();

  /**
   * Returns the 1st component.
   */
  public double getY();

}
