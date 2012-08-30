package org.au.tonomy.shared.ot;
/**
 * A document with additional methods for tracking and applying changes.
 */
public interface IMutableDocument extends IDocument {

  /**
   * Apply the given transformation to the contents of this document.
   */
  public void apply(Transform transform);

}
