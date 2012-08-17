package org.au.tonomy.shared.syntax;

import org.au.tonomy.shared.syntax.IToken.Type;

/**
 * Factory interface for producing the different kinds of tokens.
 * Producing a token should be side-effect free -- essentially it
 * should be impossible to detect if a token is produced and then
 * discarded immediately.
 */
public interface ITokenFactory<T> {

  public T newSpace(String value);

  public T newNewline(char value);

  public T newComment(String value);

  public T newWord(String value);

  public T newIdentifier(String value);

  public T newNumber(String value);

  public T newOperator(String value);

  public T newError(char value);

  public T newEof();

  public T newPunctuation(Type type);

}