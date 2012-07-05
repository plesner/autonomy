package org.au.tonomy.shared.runtime;

import org.au.tonomy.shared.syntax.Ast;
/**
 * A convenience wrapper for executing an ast.
 */
public class Executor {

  private static final IScope BOTTOM = new IScope() {
    @Override
    public IValue getValue(String name, Context context) {
      return context.getGlobal(name);
    }
  };

  private final Ast main;

  public Executor(Ast main) {
    this.main = main;
  }

  /**
   * Executes the ast in the given context.
   */
  public IValue execute(Context context) {
    return main.run(context, BOTTOM);
  }

}
