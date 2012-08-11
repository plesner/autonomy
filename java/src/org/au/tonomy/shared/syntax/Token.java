package org.au.tonomy.shared.syntax;


/**
 * A single token within some source code.
 */
public class Token implements IToken {

  private final Type type;
  private final String value;

  protected Token(Type type, String value) {
    this.type = type;
    this.value = value;
  }

  public String getCategory() {
    return type.getCategory();
  }

  @Override
  public boolean is(Type type) {
    return this.type == type;
  }

  @Override
  public Type getType() {
    return this.type;
  }

  @Override
  public String getValue() {
    return this.value;
  }

  /**
   * Returns the singleton token factory.
   */
  public static IFactory<Token> getFactory() {
    return FACTORY;
  }

  private static final IFactory<Token> FACTORY = new IFactory<Token>() {

    @Override
    public Token newEther(String value) {
      return new Token(Type.ETHER, value);
    }

    @Override
    public Token newWord(String value) {
      return new Token(Type.WORD, value);
    }

    @Override
    public Token newIdentifier(String value) {
      return new Token(Type.IDENTIFIER, value);
    }

    @Override
    public Token newNumber(String value) {
      return new Token(Type.NUMBER, value);
    }

    @Override
    public Token newOperator(String value) {
      return new Token(Type.OPERATOR, value);
    }

    @Override
    public Token newError(char value) {
      return new Token(Type.ERROR, Character.toString(value));
    }

    @Override
    public Token newEof() {
      return new Token(Type.EOF, null);
    }

    @Override
    public Token newPunctuation(Type type) {
      return new Token(type, type.toString());
    }

  };

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
    return type.name() + "(" + this.value + ")";
  }

}
