package org.au.tonomy.shared.util;

/**
 * Abstract interface for a matrix.
 */
public interface IMatrix<V4 extends IVector> {

  public V4 multiply(double x, double y, double z, double w);

}
