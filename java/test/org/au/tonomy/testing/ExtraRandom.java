package org.au.tonomy.testing;

import java.util.List;
import java.util.Random;
/**
 * A random with a few extra useful methods.
 */
@SuppressWarnings("serial")
public class ExtraRandom extends Random {

  public ExtraRandom(long seed) {
    super(seed);
  }

  /**
   * Picks a random element from the given list.
   */
  public <T> T nextElement(List<T> elms) {
    return elms.get(nextInt(elms.size()));
  }

  private static final String LETTERS = "abcdefghijklmnopqrstuvwxyz";

  /**
   * Returns the next alphabetical word of the given length.
   */
  public String nextWord(int length) {
    StringBuilder buf = new StringBuilder();
    for (int i = 0; i < length; i++)
      buf.append(LETTERS.charAt(nextInt(LETTERS.length())));
    return buf.toString();
  }

}
