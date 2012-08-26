package org.au.tonomy.testing;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import junit.framework.Assert;

import org.au.tonomy.shared.ot.Operation;
import org.au.tonomy.shared.ot.Transform;
import org.au.tonomy.shared.ot.TransformBuilder;
import org.au.tonomy.shared.syntax.Token;
import org.au.tonomy.shared.syntax.Tokenizer;
import org.au.tonomy.shared.util.Factory;
import org.au.tonomy.shared.world.Hex;

public class TestUtils extends Assert {

  /**
   * A very small quantity.
   */
  public static final double EPSILON = 1e-10;

  /**
   * A convenient abbreviation for the inner radius.
   */
  public static final double A = Hex.INNER_RADIUS;

  /**
   * A convenient abbreviation for the inner diameter.
   */
  public static final double D = Hex.INNER_DIAMETER;

  /**
   * Assert that the value found is within epsilon of the expected
   * value.
   */
  public static void assertClose(double expected, double found) {
    assertEquals(expected, found, EPSILON);
  }

  /**
   * Create a pair as a 2-element array of doubles.
   */
  public static double[] pair(double a, double b) {
    return new double[] {a, b};
  }

  /**
   * Create a pair as a 2-element array of ints.
   */
  public static int[] pair(int a, int b) {
    return new int[] {a, b};
  }

  /**
   * Assert that the given pair of doubles has the expected entry values.
   */
  public static void assertClose(double x, double y, double[] found) {
    assertEquals(2, found.length);
    assertClose(x, found[0]);
    assertClose(y, found[1]);
  }

  /**
   * Is the given string an operator token?
   */
  public static boolean isOperator(String str) {
    for (char c : str.toCharArray()) {
      if (!Character.isLetter(c) && !Character.isDigit(c))
        return true;
    }
    return false;
  }

  /**
   * Is the given string a space token?
   */
  public static boolean isSpace(String str) {
    for (char c : str.toCharArray()) {
      if (!Tokenizer.isSpace(c))
        return false;
    }
    return true;
  }

  /**
   * Converts a list of strings into tokens, taking the token type
   * from the characters in the string -- so pure alphabetical strings
   * become words, pure spaces become ether, etc.
   */
  public static List<Token> tokens(String... values) {
    List<Token> tokens = Factory.newArrayList();
    for (String value : values) {
      if (value.length() == 1 && Tokenizer.isNewline(value.charAt(0))) {
        tokens.add(Token.getFactory().newNewline(value.charAt(0)));
      } else if (isSpace(value)) {
        tokens.add(Token.getFactory().newSpace(value));
      } else {
        tokens.add(Token.getFactory().newWord(value));
      }
    }
    return tokens;
  }

  /**
   * Helper function for creating a transformation.
   */
  public static Transform trans(Operation... ops) {
    return new Transform(Arrays.asList(ops));
  }

  /**
   * Helper function for creating an insert operation.
   */
  public static Operation ins(String str) {
    return new Operation.Insert(str);
  }

  /**
   * Helper function for creating a delete operation.
   */
  public static Operation del(String str) {
    return new Operation.Delete(str);
  }

  /**
   * Helper function for creating a skip operation.
   */
  public static Operation skp(int count) {
    return new Operation.Skip(count);
  }

  /**
   * Generate a random transformation that can be applied to the given
   * input.
   */
  public static Transform getRandomTransform(ExtraRandom random, String input) {
    TransformBuilder out = new TransformBuilder();
    // First build a list of split points where we'll transform the
    // string.
    TreeSet<Integer> splits = new TreeSet<Integer>();
    while ((splits.size() < input.length() / 6))
      splits.add(random.nextInt(input.length()));
    // Then scan through the points and generate random transformations
    // to apply in those positions.
    Iterator<Integer> splitIter = splits.iterator();
    int cursor = 0;
    while (splitIter.hasNext()) {
      int start = splitIter.next();
      out.skip(start - cursor);
      int opLimit = splitIter.hasNext() ? 2 : 1;
      switch (random.nextInt(opLimit)) {
      case 0:
        out.insert(random.nextWord(random.nextInt(2) + 3));
        cursor = start;
        break;
      case 1:
        int end = splitIter.next();
        out.delete(input.substring(start, end));
        cursor = end;
        break;
      }
    }
    out.skip(input.length() - cursor);
    Transform result = out.flush();
    assertEquals(input.length(), result.getInputLength());
    return result;
  }

}
