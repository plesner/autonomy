package autonomy.syntax;

/**
 * A single token within some source code.
 */
public class Token {

  /**
   * The different classes of tokens.
   */
  public enum Type {
    WORD, DOLLAR, OPERATOR, ERROR, NUMBER, LPAREN, RPAREN, LBRACE,
    RBRACE, SEMI, HASH, AT
  }

  private final Type type;
  private final String value;

  private Token(Type type, String value) {
    this.type = type;
    this.value = value;
  }

  /**
   * Is this token of the given type?
   */
  public boolean is(Type type) {
    return this.type == type;
  }

  /**
   * Returns the type of this token.
   */
  public Type getType() {
    return this.type;
  }

  /**
   * Returns the value of this token, null for tokens that don't have
   * a value.
   */
  public String getValue() {
    return this.value;
  }

  /**
   * Factory method for creating word tokens.
   */
  public static Token word(String value) {
    return new Token(Type.WORD, value);
  }

  /**
   * Factory method for creating numbers.
   */
  public static Token number(String value) {
    return new Token(Type.NUMBER, value);
  }

  /**
   * Factory method for creating operators.
   */
  public static Token operator(String value) {
    return new Token(Type.OPERATOR, value);
  }

  /**
   * Factory method for creating error tokens.
   */
  public static Token error() {
    return new Token(Type.ERROR, null);
  }

  /**
   * Factory method for creating punctuation.
   */
  public static Token punctuation(Type type) {
    return new Token(type, null);
  }

  @Override
  public int hashCode() {
    return type.hashCode() ^ (value == null ? 0 : value.hashCode());
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof Token)) {
      return false;
    } else {
      Token that = (Token) obj;
      if (that.type != this.type) {
        return false;
      } else if (that.value == this.value) {
        return true;
      } else {
        return (that.value != null) && that.value.equals(this.value);
      }
    }
  }

  @Override
  public String toString() {
    return type + "(" + this.value + ")";
  }

}
