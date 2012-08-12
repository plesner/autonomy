package org.au.tonomy.shared.syntax;

import java.util.List;

import org.au.tonomy.shared.syntax.IToken.IFactory;
import org.au.tonomy.shared.syntax.IToken.Type;
import org.au.tonomy.shared.util.Assert;
import org.au.tonomy.shared.util.Factory;
import org.au.tonomy.shared.util.Internal;

/**
 * Utility for chopping a string into tokens.
 */
public class Tokenizer<T extends IToken> {

  private final String source;
  private final IFactory<T> tokenFactory;
  private int cursor;
  private char current;

  private Tokenizer(String source, IFactory<T> tokenFactory) {
    this.source = source;
    this.tokenFactory = tokenFactory;
    this.cursor = -1;
    this.advance();
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
    return current;
  }

  /**
   * Returns true if we're at the second character of a pair (that is,
   * we're not at the very beginning or end) and that pair is different
   * from the specified values.
   */
  private boolean atDifferentPair(char first, char second) {
    boolean isInsideSource = hasMore() && (cursor > 0);
    return isInsideSource
        && ((source.charAt(cursor - 1) != first)
            || getCurrent() != second);
  }

  /**
   * Advances to the next character.
   */
  private void advance() {
    this.cursor++;
    this.current = hasMore() ? source.charAt(this.cursor) : '\0';
  }

  /**
   * Can this character occur first in a word?
   */
  private static boolean isWordStart(char c) {
    return Character.isLetter(c) || (c == '_');
  }

  /**
   * Can this character occur after the first character of a word?
   */
  private static boolean isWordPart(char c) {
    return isWordStart(c) || Character.isDigit(c);
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
    return isOperatorStart(c) || isWordStart(c);
  }

  /**
   * Returns true if the given operation can occur as part of an operator.
   */
  private static boolean isOperatorStart(char c) {
    switch (c) {
    case '.': case '+': case '-': case '*': case '/': case '<': case '>':
    case '=': case '%':
      return true;
    default:
      return false;
    }
  }

  @SuppressWarnings("deprecation")
  @Internal
  public static boolean isSpace(char c) {
    // GWT doesn't support the non-deprecated character space predicates
    // so use this one.
    return Character.isSpace(c);
  }

  /**
   * Is this a whitespace but not a newline?
   */
  public static boolean isSpaceNotNewline(char c) {
    return isSpace(c) && !isNewline(c);
  }

  /**
   * Does this character terminate end-of-line comments?
   */
  public static boolean isNewline(char c) {
    return (c == '\n') || (c == '\r') || (c == '\f');
  }

  /**
   * Advances over the next token or tokens, adding them to the given
   * list.
   */
  private void addNext(List<T> out) {
    Assert.that(hasMore());
    T result;
    if (isNewline(getCurrent())) {
      result = tokenFactory.newNewline(getCurrent());
      advance();
    } else if (isSpace(getCurrent())) {
      result = scanSpace();
    } else if (isWordStart(getCurrent())) {
      result = scanWord();
    } else if (isNumberStart(getCurrent())) {
      result = scanNumber();
    } else if (isOperatorStart(getCurrent())) {
      result = scanOperator();
    } else {
      switch (getCurrent()) {
      case '$':
        result = scanIdentifier();
        break;
      case '(':
        result = tokenFactory.newPunctuation(Type.LPAREN);
        advance();
        break;
      case ')':
        result = tokenFactory.newPunctuation(Type.RPAREN);
        advance();
        break;
      case '[':
        result = tokenFactory.newPunctuation(Type.LBRACK);
        advance();
        break;
      case ']':
        result = tokenFactory.newPunctuation(Type.RBRACK);
        advance();
        break;
      case '{':
        result = tokenFactory.newPunctuation(Type.LBRACE);
        advance();
        break;
      case '}':
        result = tokenFactory.newPunctuation(Type.RBRACE);
        advance();
        break;
      case ';':
        result = tokenFactory.newPunctuation(Type.SEMI);
        advance();
        break;
      case ',':
        result = tokenFactory.newPunctuation(Type.COMMA);
        advance();
        break;
      case '#':
        advance();
        switch (getCurrent()) {
        case '#': case '@': case '-':
          result = scanEndOfLineComment(cursor - 1);
          break;
        case '{':
          addBlockComment(cursor - 1, out);
          return;
        default:
          result = tokenFactory.newPunctuation(Type.HASH);
          break;
        }
        break;
      case ':':
        advance();
        switch (getCurrent()) {
        case '=':
          result = tokenFactory.newPunctuation(Type.ASSIGN);
          advance();
          break;
        default:
          result = tokenFactory.newPunctuation(Type.COLON);
          break;
        }
        break;
      case '@':
        result = tokenFactory.newPunctuation(Type.AT);
        advance();
        break;
      default:
        result = tokenFactory.newError(getCurrent());
        advance();
        break;
      }
    }
    out.add(result);
  }

  private T scanSpace() {
    int start = cursor;
    while (hasMore() && isSpaceNotNewline(getCurrent()))
      advance();
    String value = source.substring(start, cursor);
    return tokenFactory.newSpace(value);
  }

  private T scanEndOfLineComment(int start) {
    while (hasMore() && !isNewline(getCurrent()))
      advance();
    String value = source.substring(start, cursor);
    return tokenFactory.newSpace(value);
  }

  private void addBlockComment(int start, List<T> out) {
    // We always want newlines to be in a separate token so we scan
    // the whole block comment in one go, yielding multiple tokens.
    int lineStart = start;
    while (atDifferentPair('}', '#')) {
      if (isNewline(getCurrent())) {
        if (lineStart != cursor) {
          String value = source.substring(lineStart, cursor);
          out.add(tokenFactory.newComment(value));
        }
        out.add(tokenFactory.newNewline(getCurrent()));
        lineStart = cursor + 1;
      }
      advance();
    }
    // If we reached the end we're at the final '#' so we advance past
    // it.
    if (hasMore())
      advance();
    String value = source.substring(lineStart, cursor);
    out.add(tokenFactory.newComment(value));
  }

  /**
   * Advances over the current word token.
   */
  private T scanWord() {
    int start = cursor;
    while (hasMore() && isWordPart(getCurrent()))
      advance();
    String value = source.substring(start, cursor);
    return tokenFactory.newWord(value);
  }

  private T scanIdentifier() {
    int start = cursor;
    advance();
    while (hasMore() && isWordPart(getCurrent()))
      advance();
    String value = source.substring(start, cursor);
    return tokenFactory.newIdentifier(value);
  }

  /**
   * Advances over the current number.
   */
  private T scanNumber() {
    int start = cursor;
    while (hasMore() && isNumberPart(getCurrent()))
      advance();
    String value = source.substring(start, cursor);
    return tokenFactory.newNumber(value);
  }

  /**
   * Advances over the current operator;
   * @return
   */
  private T scanOperator() {
    int start = cursor;
    while (hasMore() && isOperatorPart(getCurrent()))
      advance();
    String value = source.substring(start, cursor);
    return tokenFactory.newOperator(value);
  }

  /**
   * Returns the tokens of the string held by this tokenizer.
   */
  private List<T> tokenize() {
    List<T> tokens = Factory.newArrayList();
    while (hasMore()) {
      addNext(tokens);
    }
    return tokens;
  }

  /**
   * Returns the tokens of the given input string.
   */
  public static <T extends IToken> List<T> tokenize(String source, IFactory<T> tokenFactory) {
    return new Tokenizer<T>(source, tokenFactory).tokenize();
  }

}
