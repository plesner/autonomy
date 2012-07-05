package org.au.tonomy.shared.runtime;



public class IntegerValue extends AbstractValue {

  private static final MethodRegister<IntegerValue> METHODS = new MethodRegister<IntegerValue>() {{
    addMethod("+", new IMethod<IntegerValue>() {
      @Override
      public IValue invoke(IntegerValue self, IValue[] args) {
        return IntegerValue.get(self.value + args[0].getIntValue());
      }
    });
    addMethod("-", new IMethod<IntegerValue>() {
      @Override
      public IValue invoke(IntegerValue self, IValue[] args) {
        return IntegerValue.get(self.value - args[0].getIntValue());
      }
    });
    addMethod("*", new IMethod<IntegerValue>() {
      @Override
      public IValue invoke(IntegerValue self, IValue[] args) {
        return IntegerValue.get(self.value * args[0].getIntValue());
      }
    });
    addMethod("<", new IMethod<IntegerValue>() {
      @Override
      public IValue invoke(IntegerValue self, IValue[] args) {
        return BooleanValue.get(self.value < args[0].getIntValue());
      }
    });
    addMethod("=", new IMethod<IntegerValue>() {
      @Override
      public IValue invoke(IntegerValue self, IValue[] args) {
        return BooleanValue.get(self.value == args[0].getIntValue());
      }
    });
  }};

  private final int value;

  private IntegerValue(int value) {
    this.value = value;
  }

  public static IntegerValue get(int value) {
    return new IntegerValue(value);
  }

  @Override
  public int getIntValue() {
    return value;
  }

  @Override
  public IValue invoke(String method, IValue[] args) {
    return METHODS.invoke(method, this, args);
  }

  @Override
  public String toString() {
    return Integer.toString(value);
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
