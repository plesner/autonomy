package org.au.tonomy.shared.runtime;


public class BooleanValue extends AbstractValue {

  private static final MethodRegister<BooleanValue> METHODS = new MethodRegister<BooleanValue>() {{
  }};

  private static final BooleanValue TRUE = new BooleanValue(true);
  private static final BooleanValue FALSE = new BooleanValue(false);

  private final boolean value;

  private BooleanValue(boolean value) {
    this.value = value;
  }

  public static BooleanValue get(boolean value) {
    return value ? TRUE : FALSE;
  }

  @Override
  public IValue invoke(String name, IValue[] args) {
    return METHODS.invoke(name, this, args);
  }

  @Override
  public boolean isTruthy() {
    return value;
  }

  @Override
  public String toString() {
    return "#<" + value + ">";
  }

  @Override
  public boolean equals(Object obj) {
    return this == obj;
  }

}
