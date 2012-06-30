package org.au.tonomy.client.presentation;

import org.au.tonomy.shared.util.IMatrix;
import org.au.tonomy.shared.util.IVector;

/**
 * Abstract interface for a world renderer.
 */
public interface ICamera<V4 extends IVector, M4 extends IMatrix<V4>> {

  /**
   * Returns the width of the canvas we're drawing on.
   */
  public double getCanvasWidth();

  /**
   * Returns the height of the canvas we're drawing on.
   */
  public double getCanvasHeight();

  /**
   * Returns the inverse of the perspective matrix currently being
   * used.
   */
  public M4 getInversePerspective();

}
