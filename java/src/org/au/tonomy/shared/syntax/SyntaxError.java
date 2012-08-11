package org.au.tonomy.shared.syntax;
/**
 * Exception that signals a syntax error.
 */
public class SyntaxError extends Exception {

  private static final long serialVersionUID = 4941633252289190356L;

  private final IToken token;

  public SyntaxError(IToken token) {
    super(token.toString());
    this.token = token;
  }

  public IToken getToken() {
    return token;
  }

}
