package org.au.tonomy.shared.runtime;



/**
 * The singleton null value type.
 */
public class NullValue extends AbstractValue {

  private static final MethodRegister<NullValue> METHODS = new MethodRegister<NullValue>() {{
  }};

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
  public IValue invoke(String name, IValue[] args) {
    return METHODS.invoke(name, this, args);
  }

}
