package org.au.tonomy.shared.runtime;

import java.util.List;

/**
 * The singleton null value type.
 */
public class NullValue implements IValue {

  private static final NullValue INSTANCE = new NullValue();

  private NullValue() { }

  public static NullValue get() {
    return INSTANCE;
  }

  @Override
  public String toString() {
    return "#<null>";
  }

  @Override
  public IValue invoke(String method, List<IValue> args, IScope scope) {
    return NullValue.get();
  }

}
