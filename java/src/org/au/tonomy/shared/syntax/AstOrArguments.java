package org.au.tonomy.shared.syntax;

import java.util.List;

public interface AstOrArguments {

  /**
   * If this ast is used as arguments to an invocation, which arguments
   * does it represent?
   */
  public List<Ast> asArguments();

  /**
   * If this ast is used as a plain expression, which expression does
   * it represent?
   */
  public Ast asAst();

}
