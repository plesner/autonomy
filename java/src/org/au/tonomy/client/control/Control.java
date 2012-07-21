package org.au.tonomy.client.control;

import org.au.tonomy.shared.runtime.Context;
import org.au.tonomy.shared.runtime.Delegate;
import org.au.tonomy.shared.runtime.Executor;
import org.au.tonomy.shared.runtime.IValue;
import org.au.tonomy.shared.runtime.IntegerValue;
import org.au.tonomy.shared.syntax.Compiler;
import org.au.tonomy.shared.syntax.SyntaxError;
import org.au.tonomy.shared.util.Assert;
import org.au.tonomy.shared.util.Exceptions;

import com.google.gwt.core.client.GWT;
/**
 * A wrapper around the control script that also manages the environment
 * the script is run in.
 */
public class Control {

  private static final IControlBundle LIBS = GWT.create(IControlBundle.class);

  private final Context context;
  private final Delegate main;

  public Control(Context context, Delegate main) {
    this.context = context;
    this.main = main;
  }

  public IValue invoke(int step, IValue... args) {
    context.bind("$step", IntegerValue.get(step));
    return main.invoke(args);
  }

  public static Control load() {
    Context context = new Context();
    Executor exec;
    try {
      exec = Compiler.compile(context, LIBS.getControlScript().getText());
    } catch (SyntaxError se) {
      throw Exceptions.propagate(se);
    }
    exec.execute();
    IValue main = exec.getModule().getGlobal("$main");
    Assert.notNull(main);
    return new Control(context, Delegate.create(main, "()"));
  }

}
