package org.au.tonomy.shared.runtime;

import java.util.List;

/**
 * Abstract interface for runtime values.
 */
public interface IValue {

  /**
   * Invokes the given method on this value, returning the result.
   */
  public IValue invoke(String method, List<IValue> args, IScope scope);

}
