package org.au.tonomy.shared.runtime;

import java.util.List;

import org.au.tonomy.shared.syntax.Ast;

public class LambdaValue extends AbstractValue {

  private static final MethodRegister<LambdaValue> METHODS = new MethodRegister<LambdaValue>() {{
    addMethod("()", new IMethod<LambdaValue>() {
      @Override
      public IValue invoke(final LambdaValue self, final IValue[] args) {
        return self.body.run(self.context, new IScope() {
          @Override
          public IValue getValue(String name, Context context) {
            for (int i = 0; i < self.params.size(); i++) {
              if (name.equals(self.params.get(i)))
                return args[i];
            }
            return self.scope.getValue(name, context);
          }
        });
      }
    });
  }};

  private final List<String> params;
  private final Ast body;
  private final IScope scope;
  private final Context context;

  public LambdaValue(List<String> params, Ast body, IScope scope, Context context) {
    this.params = params;
    this.body = body;
    this.scope = scope;
    this.context = context;
  }

  @Override
  public IValue invoke(String name, IValue[] args) {
    return METHODS.invoke(name, this, args);
  }

}
