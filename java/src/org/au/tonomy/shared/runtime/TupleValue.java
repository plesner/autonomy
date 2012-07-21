package org.au.tonomy.shared.runtime;
/**
 * A tuple.
 */
public class TupleValue extends AbstractValue {

  private static final MethodRegister<TupleValue> METHODS = new MethodRegister<TupleValue>() {{
    addMethod("[]", new IMethod<TupleValue>() {
      @Override
      public IValue invoke(TupleValue self, IValue[] args) {
        return self.elms[args[0].getIntValue()];
      }
    });
    addMethod(".length", new IMethod<TupleValue>() {
      @Override
      public IValue invoke(TupleValue self, IValue[] args) {
        return IntegerValue.get(self.elms.length);
      }
    });
  }};

  private final IValue[] elms;

  public TupleValue(IValue[] elms) {
    this.elms = elms;
  }

  @Override
  public IValue invoke(String name, IValue[] args) {
    return METHODS.invoke(name, this, args);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    } else if (!(obj instanceof TupleValue)) {
      return false;
    } else {
      IValue[] mine = elms;
      IValue[] others = ((TupleValue) obj).elms;
      if (mine.length != others.length)
        return false;
      for (int i = 0; i < mine.length; i++) {
        if (!mine[i].equals(others[i]))
          return false;
      }
      return true;
    }
  }

}
