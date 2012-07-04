package org.au.tonomy.shared.runtime;

public abstract class AbstractValue implements IValue {

  @Override
  public boolean isTruthy() {
    return false;
  }

}
