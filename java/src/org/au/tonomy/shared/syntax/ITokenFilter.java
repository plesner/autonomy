package org.au.tonomy.shared.syntax;

import java.util.List;

/**
 * A filter that can be given raw string events and transform them into
 * token events on the stream of tokens corresponding to the input.
 */
public interface ITokenFilter<T extends IToken> {

  /**
   * A listener for token events.
   */
  public interface ITokenListener<T extends IToken> {

    /**
     * The given tokens were inserted at the given offset.
     */
    public void onInsert(int offset, List<T> inserted);

    /**
     * The given number of tokens were removed at the given offset.
     */
    public void onRemove(int offset, List<T> removed);

    /**
     * The given tokens were inserted instead of the specified number
     * of tokens, starting at the given offset.
     */
    public void onReplace(int offset, List<T> removed, List<T> inserted);

  }

  /**
   * Add a listener to the set managed by this filter.
   */
  public void addListener(ITokenListener<T> listener);

  /**
   * Inserts the given string at the end of the source.
   */
  public void append(String str);

  /**
   * Inserts the given string at the specified offset.
   */
  public void insert(int offset, String str);

  /**
   * Removes the part of the contents starting at 'from' and removing
   * 'length' characters.
   */
  public void delete(int from, int length);

  /**
   * Identical to a delete followed by an insert, except the operation
   * happens atomically.
   */
  public void replace(int from, int length, String replacement);

}
