package org.au.tonomy.shared.runtime;

import java.util.ArrayList;
import java.util.List;


public class HereValue extends AbstractValue {

  private static final MethodRegister<HereValue> METHODS = new MethodRegister<HereValue>() {{
    addMethod(".annotated", new IMethod<HereValue>() {
      @Override
      public IValue invoke(HereValue self, IValue[] args) {
        IValue annotation = args[0];
        List<IValue> values = new ArrayList<IValue>();
        self.scope.addAnnotated(annotation, values);
        return new TupleValue(values.toArray(new IValue[values.size()]));
      }
    });
  }};

  private final ModuleValue module;
  private final IScope scope;

  public HereValue(ModuleValue module, IScope scope) {
    this.module = module;
    this.scope = scope;
  }

  @Override
  public IValue invoke(String name, IValue[] args) {
    return METHODS.invoke(name, this, args);
  }

}
