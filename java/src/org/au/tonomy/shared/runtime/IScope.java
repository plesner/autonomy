package org.au.tonomy.shared.runtime;

/**
 * A dynamic scope which is responsible for resolving names.
 */
public interface IScope {

  /**
   * Returns the value bound to the given name, using the given context
   * to resolve global names.
   */
  public IValue getValue(Object name, Context context);

}
