package org.au.tonomy.shared.ot;

import org.au.tonomy.shared.util.Assert;

/**
 * A stream of string input that can be transformed by a transformation.
 */
public class StringInput {

  private final String str;
  private int cursor = 0;

  public StringInput(String str) {
    this.str = str;
  }

  /**
   * Has all input been read from this stream?
   */
  public boolean isDone() {
    return cursor == str.length();
  }

  /**
   * Skips over the next part of the input, which must be the specified
   * string.
   */
  public void skip(String text) {
    int targetCursor = cursor + text.length();
    Assert.that(targetCursor <= str.length());
    if (Assert.enableExpensiveAssertions)
      Assert.equals(text, str.substring(cursor, targetCursor));
    this.cursor = targetCursor;
  }

  /**
   * Scans over the next count characters, returning them as a string.
   */
  public String scan(int count) {
    int targetCursor = cursor + count;
    Assert.that(targetCursor <= str.length());
    String result = str.substring(cursor, targetCursor);
    this.cursor = targetCursor;
    return result;
  }

}
