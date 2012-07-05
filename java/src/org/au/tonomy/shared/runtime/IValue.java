package org.au.tonomy.shared.runtime;


/**
 * Abstract interface for runtime values.
 */
public interface IValue {

  /**
   * Invokes the given method on this value, returning the result.
   */
  public IValue invoke(String name, IValue[] args);

  /**
   * Is this value truthy?
   */
  public boolean isTruthy();

  /**
   * Returns the integer value of this value if this is an integer,
   * otherwise throws an exception.
   */
  public int getIntValue();

}
