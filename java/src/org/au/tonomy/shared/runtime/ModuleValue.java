package org.au.tonomy.shared.runtime;

import java.util.HashMap;
import java.util.Map;


public class ModuleValue extends AbstractValue {

  private static final MethodRegister<ModuleValue> METHODS = new MethodRegister<ModuleValue>() {{
  }};

  private final Context context;
  private final Map<Object, IValue> bindings = new HashMap<Object, IValue>();

  public ModuleValue(Context context) {
    this.context = context;
  }

  @Override
  public IValue invoke(String name, IValue[] args) {
    return METHODS.invoke(name, this, args);
  }

  public void bind(Object name, IValue value) {
    this.bindings.put(name, value);
  }

  public IValue getGlobal(Object name) {
    IValue binding = bindings.get(name);
    return (binding == null) ? context.getGlobal(name) : binding;
  }

}
