package org.au.tonomy.shared.util;
/**
 * An object that represents the inverse of another action.
 */
public interface IUndo {

  /**
   * Undoes the previous operation.
   */
  public void undo();

}
