package org.au.tonomy.shared.source;
/**
 * A tree of directories and source files.
 */
public interface ISourceTree {

  /**
   * Returns the root entry of this source tree.
   */
  public ISourceEntry getRoot();

}
