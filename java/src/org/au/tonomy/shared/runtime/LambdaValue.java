package org.au.tonomy.shared.runtime;

import org.au.tonomy.shared.syntax.Ast;

public class LambdaValue extends AbstractValue {

  private static final MethodRegister<LambdaValue> METHODS = new MethodRegister<LambdaValue>() {{
    addMethod(".call", new IMethod<LambdaValue>() {
      @Override
      public IValue invoke(LambdaValue self, IValue[] args) {
        return self.body.run(self.scope, self.context);
      }
    });
  }};

  private final Ast body;
  private final IScope scope;
  private final Context context;

  public LambdaValue(Ast body, IScope scope, Context context) {
    this.body = body;
    this.scope = scope;
    this.context = context;
  }

  @Override
  public IValue invoke(String name, IValue[] args) {
    return METHODS.invoke(name, this, args);
  }

}
