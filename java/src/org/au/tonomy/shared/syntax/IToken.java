package org.au.tonomy.shared.syntax;



/**
 * A factory type for producing token objects.
 */
public interface IToken {

  /**
   * The different classes of tokens.
   */
  public enum Type {

    WORD      (null, "word"),
    OPERATOR  (null, "operator"),
    ERROR     (null, "error"),
    EOF       (null, "eof"),
    NUMBER    (null, "number"),
    IDENTIFIER(null, "identifier"),
    ETHER     (null, "ether"),
    LPAREN    ("(",  "punctuation"),
    RPAREN    (")",  "punctuation"),
    LBRACK    ("[",  "punctuation"),
    RBRACK    ("]",  "punctuation"),
    LBRACE    ("{",  "punctuation"),
    RBRACE    ("}",  "punctuation"),
    SEMI      (";",  "punctuation"),
    COMMA     (",",  "punctuation"),
    HASH      ("#",  "punctuation"),
    ASSIGN    (":=", "punctuation"),
    COLON     (":",  "punctuation"),
    AT        ("@",  "punctuation");

    private final String value;
    private final String category;

    private Type(String value, String category) {
      this.value = value;
      this.category = category;
    }

    @Override
    public String toString() {
      return value;
    }

    public String getCategory() {
      return this.category;
    }

  }

  /**
   * Factory interface for producing the different kinds of tokens.
   */
  public interface IFactory<T extends IToken> {

    public T newEther(String value);

    public T newWord(String value);

    public T newIdentifier(String value);

    public T newNumber(String value);

    public T newOperator(String value);

    public T newError(char value);

    public T newEof();

    public T newPunctuation(Type type);

  }

  /**
   * Is this token of the given type?
   */
  public boolean is(Type type);

  /**
   * Returns the type of this token.
   */
  public Type getType();

  /**
   * Returns the value of this token, null for tokens that don't have
   * a value.
   */
  public String getValue();

}
