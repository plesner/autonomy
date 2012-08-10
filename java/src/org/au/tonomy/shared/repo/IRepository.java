package org.au.tonomy.shared.repo;

import org.au.tonomy.shared.runtime.IValue;

/**
 * A source repository where names can be looked up.
 */
public interface IRepository {

  /**
   * Returns the value bound to the given name in this repository.
   */
  public IValue resolve(Name name);

}
