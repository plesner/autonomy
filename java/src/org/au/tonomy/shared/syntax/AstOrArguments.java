package org.au.tonomy.shared.syntax;

import java.util.List;

public abstract class AstOrArguments {

  /**
   * If this ast is used as arguments to an invocation, which arguments
   * does it represent?
   */
  public abstract List<Ast> asArguments();

  /**
   * If this ast is used as a plain expression, which expression does
   * it represent?
   */
  public abstract Ast asAst();

}
