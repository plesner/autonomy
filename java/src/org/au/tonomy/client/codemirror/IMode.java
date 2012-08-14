package org.au.tonomy.client.codemirror;

/**
 * A language mode for codemirror.
 */
public interface IMode<S> {

  /**
   * Returns the name of the mode.
   */
  public String getName();

  /**
   * Creates a new state object to be used while parsing.
   */
  public S newStartState();

  /**
   * Returns the next token from the stream, given that we're in the
   * specified state.
   */
  public String getNextToken(Stream stream, S state);

}
