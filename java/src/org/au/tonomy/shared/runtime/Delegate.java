package org.au.tonomy.shared.runtime;
/**
 * A stand-alone method that can be invoked as if it was a function.
 */
public class Delegate {

  private final IValue recv;
  private final String method;

  private Delegate(IValue recv, String method) {
    this.recv = recv;
    this.method = method;
  }

  /**
   * Invokes the delegate with the given arguments.
   */
  public IValue invoke(IValue... args) {
    return recv.invoke(method, args);
  }

  /**
   * Creates a delegate for the given method to the given object.
   */
  public static Delegate create(IValue value, String method) {
    return new Delegate(value, method);
  }

}
