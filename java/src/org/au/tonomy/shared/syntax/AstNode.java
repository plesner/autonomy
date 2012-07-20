package org.au.tonomy.shared.syntax;
/**
 * An abstraction that maps between a parsed syntax tree and he raw
 * tokens that it was parsed from.
 */
public abstract class AstNode {

  public static class AstText extends AstNode {

    private final Token token;

    public AstText(Token token) {
      this.token = token;
    }

    @Override
    public boolean isText() {
      return true;
    }

    public String getValue() {
      return token.getValue();
    }

  }

  /**
   * Creates a new text ast node with the given contents.
   */
  public static AstNode text(Token token) {
    return new AstText(token);
  }

  /**
   * Is this a text ast node?
   */
  public boolean isText() {
    return false;
  }

}
