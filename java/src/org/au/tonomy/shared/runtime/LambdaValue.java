package org.au.tonomy.shared.runtime;

import java.util.List;

import org.au.tonomy.shared.syntax.Ast;

public class LambdaValue extends AbstractValue {

  private static final MethodRegister<LambdaValue> METHODS = new MethodRegister<LambdaValue>() {{
    addMethod("()", new IMethod<LambdaValue>() {
      @Override
      public IValue invoke(final LambdaValue self, final IValue[] args) {
        return self.body.run(self.module, new IScope() {
          @Override
          public IValue getValue(Object name, ModuleValue module) {
            for (int i = 0; i < self.params.size(); i++) {
              if (name.equals(self.params.get(i)))
                return args[i];
            }
            return self.outerScope.getValue(name, module);
          }
          @Override
          public void addAnnotated(IValue annotation, List<IValue> values) {
            self.outerScope.addAnnotated(annotation, values);
          }
        });
      }
    });
  }};

  private final List<String> params;
  private final Ast body;
  private final IScope outerScope;
  private final ModuleValue module;

  public LambdaValue(List<String> params, Ast body, IScope scope, ModuleValue module) {
    this.params = params;
    this.body = body;
    this.outerScope = scope;
    this.module = module;
  }

  @Override
  public IValue invoke(String name, IValue[] args) {
    return METHODS.invoke(name, this, args);
  }

}
