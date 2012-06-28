package org.au.tonomy.shared.syntax;

import java.util.ArrayList;
import java.util.List;

import org.au.tonomy.shared.syntax.Token.Type;
import org.au.tonomy.shared.util.Assert;



/**
 * Utility for chopping a string into tokens.
 */
public class Tokenizer {

  private final String source;
  private int cursor = 0;

  private Tokenizer(String source) {
    this.source = source;
    skipSpaces();
  }

  /**
   * Is there more input?
   */
  private boolean hasMore() {
    return cursor < source.length();
  }

  /**
   * Returns the current character.
   */
  private char getCurrent() {
    return source.charAt(cursor);
  }

  /**
   * Advances to the next character.
   */
  private void advance() {
    cursor++;
  }

  /**
   * Advances over any whitespace.
   */
  @SuppressWarnings("deprecation")
  private void skipSpaces() {
    // GWT doesn't understand isWhitespace so we use the deprecated
    // isSpace instead.
    while (hasMore() && Character.isSpace(getCurrent()))
      advance();
  }

  /**
   * Can this character occur first in a word?
   */
  private static boolean isWordStart(char c) {
    return isWordPart(c);
  }

  /**
   * Can this character occur after the first character of a word?
   */
  private static boolean isWordPart(char c) {
    return Character.isLetter(c);
  }

  /**
   * Can this character occur first in a number?
   */
  private static boolean isNumberStart(char c) {
    return Character.isDigit(c);
  }

  /**
   * Can this character occur after the first character of a number?
   */
  private static boolean isNumberPart(char c) {
    return isNumberStart(c);
  }

  /**
   * Returns true if the given operation can occur as part of an operator.
   */
  private static boolean isOperatorPart(char c) {
    switch (c) {
    case '.': case '+': case '-': case '*': case '/':
      return true;
    default:
      return false;
    }
  }

  /**
   * Advances over the next token, returning the token that was skipped.
   */
  private Token scanNext() {
    Assert.that(hasMore());
    Token result;
    if (isWordStart(getCurrent())) {
      result = scanWord();
    } else if (isNumberStart(getCurrent())) {
      result = scanNumber();
    } else if (isOperatorPart(getCurrent())) {
      result = scanOperator();
    } else {
      switch (getCurrent()) {
      case '$':
        result = Token.punctuation(Type.DOLLAR);
        break;
      case '(':
        result = Token.punctuation(Type.LPAREN);
        break;
      case ')':
        result = Token.punctuation(Type.RPAREN);
        break;
      case '{':
        result = Token.punctuation(Type.LBRACE);
        break;
      case '}':
        result = Token.punctuation(Type.RBRACE);
        break;
      case ';':
        result = Token.punctuation(Type.SEMI);
        break;
      case '#':
        result = Token.punctuation(Type.HASH);
        break;
      case '@':
        result = Token.punctuation(Type.AT);
        break;
      default:
        result = Token.error();
        break;
      }
      advance();
    }
    skipSpaces();
    return result;
  }

  /**
   * Advances over the current word token.
   */
  private Token scanWord() {
    int start = cursor;
    while (hasMore() && isWordPart(getCurrent()))
      advance();
    String value = source.substring(start, cursor);
    return Token.word(value);
  }

  /**
   * Advances over the current number.
   */
  private Token scanNumber() {
    int start = cursor;
    while (hasMore() && isNumberPart(getCurrent()))
      advance();
    String value = source.substring(start, cursor);
    return Token.number(value);
  }

  /**
   * Advances over the current operator;
   * @return
   */
  private Token scanOperator() {
    int start = cursor;
    while (hasMore() && isOperatorPart(getCurrent()))
      advance();
    String value = source.substring(start, cursor);
    return Token.operator(value);
  }

  /**
   * Returns the tokens of the string held by this tokenizer.
   */
  private List<Token> tokenize() {
    List<Token> tokens = new ArrayList<Token>();
    while (hasMore()) {
      tokens.add(scanNext());
    }
    return tokens;
  }

  /**
   * Returns the tokens of the given input string.
   */
  public static List<Token> tokenize(String source) {
    return new Tokenizer(source).tokenize();
  }

}
