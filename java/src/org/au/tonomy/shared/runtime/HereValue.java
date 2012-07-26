package org.au.tonomy.shared.runtime;



public class HereValue extends AbstractValue {

  private static final MethodRegister<HereValue> METHODS = new MethodRegister<HereValue>() {{
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
