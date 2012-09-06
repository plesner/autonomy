package org.au.tonomy.shared.plankton;


/**
 * Types that implement this interface are asked to convert themselves
 * to plankton when being serialized.
 */
public interface IPlanktonDatable {

  /**
   * Return a plankton representation of this object.
   */
  public Object toPlanktonData(IPlanktonFactory factory);

}
