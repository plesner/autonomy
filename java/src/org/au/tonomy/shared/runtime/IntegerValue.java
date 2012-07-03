package org.au.tonomy.shared.runtime;

import java.util.List;

public class IntegerValue implements IValue {

  private final int value;

  private IntegerValue(int value) {
    this.value = value;
  }

  public static IntegerValue get(int value) {
    return new IntegerValue(value);
  }

  @Override
  public String toString() {
    return Integer.toString(value);
  }

  @Override
  public IValue invoke(String method, List<IValue> args, IScope scope) {
    return NullValue.get();
  }

  @Override
  public int hashCode() {
    return value;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    } else if (!(obj instanceof IntegerValue)) {
      return false;
    } else {
      return ((IntegerValue) obj).value == value;
    }
  }

}
