package org.au.tonomy.shared.syntax;
/**
 * Exception that signals a syntax error.
 */
public class SyntaxError extends Exception {

  private static final long serialVersionUID = 4941633252289190356L;

  public SyntaxError(Token token) {
    super(token.toString());
  }

}
