package org.au.tonomy.shared.runtime;

import java.util.Map;

import org.au.tonomy.shared.util.Assert;
import org.au.tonomy.shared.util.Factory;

/**
 * Global state associated with runtime execution.
 */
public class Context {

  private final Map<String, IValue> globals = Factory.newHashMap();

  public void bind(String name, IValue value) {
    globals.put(name, value);
  }

  /**
   * Looks up a name in the global namespace.
   */
  public IValue getGlobal(Object name) {
    IValue result = globals.get(name);
    return Assert.notNull(result);
  }

}
