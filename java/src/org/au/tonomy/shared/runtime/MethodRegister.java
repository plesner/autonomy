package org.au.tonomy.shared.runtime;

import java.util.Map;

import org.au.tonomy.shared.util.Factory;

/**
 * A mapping from method names to methods.
 */
public class MethodRegister<T extends IValue> {

  /**
   * An individual method.
   */
  public static interface IMethod<T extends IValue> {

    public IValue invoke(T self, IValue[] args);

  }

  private final Map<String, IMethod<T>> methods = Factory.newHashMap();

  private IMethod<T> getMethod(String name) {
    IMethod<T> method = methods.get(name);
    if (method == null)
      throw new AssertionError("Couldn't find method " + name);
    return method;
  }

  protected void addMethod(String name, IMethod<T> method) {
    this.methods.put(name, method);
  }

  public IValue invoke(String name, T self, IValue[] args) {
    return getMethod(name).invoke(self, args);
  }

}
