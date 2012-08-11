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
   * Supertype for token factories that return the same type of token
   * for all calls, just with different arguments.
   */
  protected static abstract class AbstractFactory<T extends Token> implements IFactory<T> {

    /**
     * Subclasses can implement this to define how a token is constructed
     * independent of its type.
     */
    protected abstract T newToken(Type type, String value);

    @Override
    public T newEther(String value) {
      return newToken(Type.ETHER, value);
    }

    @Override
    public T newWord(String value) {
      return newToken(Type.WORD, value);
    }

    @Override
    public T newIdentifier(String value) {
      return newToken(Type.IDENTIFIER, value);
    }

    @Override
    public T newNumber(String value) {
      return newToken(Type.NUMBER, value);
    }

    @Override
    public T newOperator(String value) {
      return newToken(Type.OPERATOR, value);
    }

    @Override
    public T newError(char value) {
      return newToken(Type.ERROR, Character.toString(value));
    }

    @Override
    public T newEof() {
      return newToken(Type.EOF, null);
    }

    @Override
    public T newPunctuation(Type type) {
      return newToken(type, type.toString());
    }

  };

  /**
   * Singleton token factory.
   */
  private static final IFactory<Token> FACTORY = new AbstractFactory<Token>() {
    @Override
    protected Token newToken(Type type, String value) {
      return new Token(type, value);
    }
  };

  /**
   * Returns the singleton token factory.
   */
  public static IFactory<Token> getFactory() {
    return FACTORY;
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
    return type.name() + "(" + this.value + ")";
  }

}
