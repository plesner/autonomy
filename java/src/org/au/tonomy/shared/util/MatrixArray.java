package org.au.tonomy.shared.util;
/**
 * An array that can be used as the backing store for matrix math.
 */
public interface MatrixArray {

  /**
   * How long is this array?
   */
  public int getLength();

  /**
   * Returns the index'th element.
   */
  public double get(int index);

  /**
   * Sets the index'th element.
   */
  public void set(int index, double value);

}
