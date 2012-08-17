package org.au.tonomy.shared.syntax;
/**
 * A linear, finite stream of characters.
 */
public interface ICharStream {

  /**
   * Does this stream have more input?
   */
  public boolean hasMore();

  /**
   * Returns the current character.
   */
  public char getCurrent();

  /**
   * Returns the character one ahead of the current one.
   */
  public char getNext();

  /**
   * Advances to the next character.
   */
  public void advance();

  /**
   * Returns the current cursor position.
   */
  public int getCursor();

  /**
   * Returns a substring of this input.
   */
  public String substring(int start, int end);

}
