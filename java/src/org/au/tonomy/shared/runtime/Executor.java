package org.au.tonomy.shared.runtime;

import java.util.HashMap;
import java.util.Map;

import org.au.tonomy.shared.syntax.Ast;

public class Executor {

  private final Ast main;
  private final Map<String, IValue> globals = new HashMap<String, IValue>();

  public Executor(Ast main) {
    this.main = main;
  }

  public void setGlobal(String name, IValue value) {
    globals.put(name, value);
  }

  public IValue execute() {
    return main.run(new IScope() {
      @Override
      public IValue getValue(String name) {
        return globals.get(name);
      }
    });
  }

}
