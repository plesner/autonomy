package org.au.tonomy.shared.runtime;

import java.util.List;

/**
 * A dynamic scope which is responsible for resolving names.
 */
public interface IScope {

  /**
   * Returns the value bound to the given name, using the given context
   * to resolve global names.
   */
  public IValue getValue(Object name, ModuleValue module);

  /**
   * Adds all values annotated with the given annotation to the given
   * list.
   */
  public void addAnnotated(IValue annotation, List<IValue> values);

}
