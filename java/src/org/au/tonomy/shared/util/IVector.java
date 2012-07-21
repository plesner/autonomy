package org.au.tonomy.shared.util;
/**
 * Abstract vector interface.
 */
public interface IVector extends IPoint {

  /**
   * Returns the index'th component of this vector.
   */
  public double get(int index);

}
