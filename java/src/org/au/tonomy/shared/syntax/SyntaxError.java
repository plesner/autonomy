package org.au.tonomy.shared.syntax;
/**
 * Exception that signals a syntax error.
 */
public class SyntaxError extends Exception {

  private static final long serialVersionUID = 4941633252289190356L;

  private final Token token;

  public SyntaxError(Token token) {
    super(token.toString());
    this.token = token;
  }

  public Token getToken() {
    return token;
  }

}
