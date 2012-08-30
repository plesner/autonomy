package org.au.tonomy.shared.plankton;

import org.au.tonomy.shared.util.IPlanktonFactory;

/**
 * Types that implement this interface are asked to convert themselves
 * to plankton when being serialized.
 */
public interface IPlanktonable {

  /**
   * Return a plankton representation of this object.
   */
  public Object toPlankton(IPlanktonFactory factory);

}
