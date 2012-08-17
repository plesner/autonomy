package org.au.tonomy.shared.syntax;

/**
 * Abstract interface for source tokens.
 */
public interface IToken {

  /**
   * The flavor of a token -- for instance space and punctuation, of
   * which there are multiple types but all share various properties.
   */
  public enum Flavor {
    ETHER,
    PUNCTUATION,
    OTHER
  }

  /**
   * The different classes of tokens.
   */
  public enum Type {

    WORD      (null, "word",        Flavor.OTHER),
    OPERATOR  (null, "operator",    Flavor.OTHER),
    ERROR     (null, "error",       Flavor.OTHER),
    EOF       (null, "eof",         Flavor.OTHER),
    NUMBER    (null, "number",      Flavor.OTHER),
    IDENTIFIER(null, "identifier",  Flavor.OTHER),
    SPACE     (null, "space",       Flavor.ETHER),
    NEWLINE   (null, "space",       Flavor.ETHER),
    COMMENT   (null, "comment",     Flavor.ETHER),
    LPAREN    ("(",  "punctuation", Flavor.PUNCTUATION),
    RPAREN    (")",  "punctuation", Flavor.PUNCTUATION),
    LBRACK    ("[",  "punctuation", Flavor.PUNCTUATION),
    RBRACK    ("]",  "punctuation", Flavor.PUNCTUATION),
    LBRACE    ("{",  "punctuation", Flavor.PUNCTUATION),
    RBRACE    ("}",  "punctuation", Flavor.PUNCTUATION),
    SEMI      (";",  "punctuation", Flavor.PUNCTUATION),
    COMMA     (",",  "punctuation", Flavor.PUNCTUATION),
    HASH      ("#",  "punctuation", Flavor.PUNCTUATION),
    ASSIGN    (":=", "punctuation", Flavor.PUNCTUATION),
    COLON     (":",  "punctuation", Flavor.PUNCTUATION),
    AT        ("@",  "punctuation", Flavor.PUNCTUATION);

    private final Flavor flavor;
    private final String value;
    private final String category;

    private Type(String value, String category, Flavor flavor) {
      this.flavor = flavor;
      this.value = value;
      this.category = category;
    }

    @Override
    public String toString() {
      return this.value;
    }

    public String getCategory() {
      return this.category;
    }

    public Flavor getFlavor() {
      return this.flavor;
    }

    public boolean isPunctuation() {
      return getFlavor() == Flavor.PUNCTUATION;
    }

  }

  /**
   * Is this token of the given type?
   */
  public boolean is(Type type);

  /**
   * Is this token of the given flavor?
   */
  public boolean is(Flavor flavor);

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
