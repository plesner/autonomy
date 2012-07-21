package org.au.tonomy.shared.runtime;

import java.util.List;

import org.au.tonomy.shared.syntax.Ast;
/**
 * A convenience wrapper for executing an ast.
 */
public class Executor {

  private static final IScope BOTTOM = new IScope() {
    @Override
    public IValue getValue(Object name, ModuleValue module) {
      return module.getGlobal(name);
    }
    @Override
    public void addAnnotated(IValue annotation, List<IValue> values) {
      // nothing to do
    }
  };

  private final Ast main;
  private final ModuleValue module;

  public Executor(Context context, Ast main) {
    this.module = new ModuleValue(context);
    this.main = main;
  }

  /**
   * Executes the ast in the given context.
   */
  public IValue execute() {
    return main.run(module, BOTTOM);
  }

  /**
   * Returns the global namespace module used by this executor.
   */
  public ModuleValue getModule() {
    return this.module;
  }

}
