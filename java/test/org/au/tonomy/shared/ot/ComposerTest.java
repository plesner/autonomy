package org.au.tonomy.shared.ot;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;
import java.util.TreeSet;

import junit.framework.TestCase;

import org.au.tonomy.shared.util.Pair;
import org.junit.Test;

public class ComposerTest extends TestCase {

  private void checkXform(String original, Transform a, Transform b,
      String expected, Transform aPrime, Transform bPrime) {
    // First check that we produced the right xformed transformations.
    Pair<Transform, Transform> prime = Composer.xform(a, b);
    assertEquals(aPrime, prime.getFirst());
    assertEquals(bPrime, prime.getSecond());
    // Then check that applying them has the expected effect.
    String aStr = a.call(original);
    String aAfter = prime.getSecond().call(aStr);
    assertEquals(expected, aAfter);
    String bStr = b.call(original);
    String bAfter = prime.getFirst().call(bStr);
    assertEquals(expected, bAfter);
    // Exercise the undo transformations.
    Transform aInv = a.getInverse();
    assertEquals(original, aInv.call(aStr));
    Transform bInv = b.getInverse();
    assertEquals(original, bInv.call(bStr));
  }

  @Test
  public void testXformInsert() {
    checkXform(
        "",
        trans(ins("foo")),
        trans(ins("foobar")),
        "foobar",
        trans(skp(6)),
        trans(skp(3), ins("bar")));
    checkXform(
        "",
        trans(ins("fooxar")),
        trans(ins("foobar")),
        "foobxar",
        trans(skp(4), ins("x"), skp(2)),
        trans(skp(3), ins("b"), skp(3)));
    checkXform(
        "",
        trans(ins("bar")),
        trans(ins("foobar")),
        "bfaoorbar",
        trans(ins("b"), skp(1), ins("a"), skp(2), ins("r"), skp(3)),
        trans(skp(1), ins("f"), skp(1), ins("oo"), skp(1), ins("bar")));
    checkXform(
        "",
        trans(ins("ace")),
        trans(ins("bdf")),
        "abcdef",
        trans(ins("a"), skp(1), ins("c"), skp(1), ins("e"), skp(1)),
        trans(skp(1), ins("b"), skp(1), ins("d"), skp(1), ins("f")));
  }

  @Test
  public void testXformSkip() {
    checkXform(
        "foo",
        trans(skp(2), ins("x"), skp(1)),
        trans(skp(1), ins("y"), skp(2)),
        "fyoxo",
        trans(skp(3), ins("x"), skp(1)),
        trans(skp(1), ins("y"), skp(3)));
  }

  @Test
  public void testXformDelete() {
    checkXform(
        "foobar",
        trans(skp(2), del("ob"), skp(2)),
        trans(skp(2), del("oba"), skp(1)),
        "for",
        trans(skp(3)),
        trans(skp(2), del("a"), skp(1)));
    checkXform(
        "abcdef",
        trans(del("abc"), skp(3)),
        trans(skp(3), del("def")),
        "",
        trans(del("abc")),
        trans(del("def")));
    checkXform(
        "abdef",
        trans(skp(2), ins("c"), skp(3)),
        trans(del("ab"), skp(3)),
        "cdef",
        trans(ins("c"), skp(3)),
        trans(del("ab"), skp(4)));
    checkXform(
        "abdef",
        trans(skp(2), ins("c"), skp(3)),
        trans(skp(1), del("b"), skp(3)),
        "acdef",
        trans(skp(1), ins("c"), skp(3)),
        trans(skp(1), del("b"), skp(4)));
  }

  @Test
  public void testXformDeleteInsert() {
    checkXform(
        "foo",
        trans(ins("bla"), skp(3)),
        trans(del("fo"), skp(1)),
        "blao",
        trans(ins("bla"), skp(1)),
        trans(skp(3), del("fo"), skp(1)));
  }

  @Test
  public void testRandomizedRegression() {
    checkXform(
        "abcdefghi",
        trans(skp(3), ins("jklmno"), skp(6)),
        trans(skp(1), del("bcdefgh"), skp(1)),
        "ajklmnoi",
        trans(skp(1), ins("jklmno"), skp(1)),
        trans(skp(1), del("bc"), skp(6), del("defgh"), skp(1)));
    checkXform(
        "abcdefghijklmnopqrstu",
        trans(skp(5), ins("vwxyz0"), skp(6), ins("123456"), skp(10)),
        trans(skp(2), del("c"), skp(7), del("klmnopqrst"), skp(1)),
        "abdevwxyz0fghij123456u",
        trans(skp(4), ins("vwxyz0"), skp(5), ins("123456"), skp(1)),
        trans(skp(2), del("c"), skp(13), del("k"), skp(6), del("lmnopqrst"), skp(1)));
  }

  private void checkCompose(String original, Transform a, Transform b,
      Transform composed, String expected) {
    assertEquals(expected, b.call(a.call(original)));
    Transform found = Composer.compose(a, b);
    assertEquals(composed, found);
    assertEquals(expected, composed.call(original));
  }

  @Test
  public void testCompose() {
    checkCompose(
        "",
        trans(ins("abc")),
        trans(skp(3), ins("def")),
        trans(ins("abcdef")),
        "abcdef");
    checkCompose(
        "abcdef",
        trans(skp(3), ins("y"), skp(3)),
        trans(skp(3), ins("x"), skp(1), ins("z"), skp(3)),
        trans(skp(3), ins("xyz"), skp(3)),
        "abcxyzdef");
    checkCompose(
        "abcdef",
        trans(ins("("), skp(6), ins(")")),
        trans(skp(4), ins("-"), skp(4)),
        trans(ins("("), skp(3), ins("-"), skp(3), ins(")")),
        "(abc-def)");
    checkCompose(
        "abcdef",
        trans(skp(2), del("cd"), skp(2)),
        trans(ins("("), skp(4), ins(")")),
        trans(ins("("), skp(2), del("cd"), skp(2), ins(")")),
        "(abef)");
    checkCompose(
        "abcdef",
        trans(skp(3), ins("."), skp(3)),
        trans(skp(2), del("c.d"), skp(2)),
        trans(skp(2), del("cd"), skp(2)),
        "abef");
    checkCompose(
        "abcdef",
        trans(del("abc"), skp(3)),
        trans(ins("xyz"), skp(3)),
        trans(del("abc"), ins("xyz"), skp(3)),
        "xyzdef");
    checkCompose(
        "abcdef",
        trans(del("a"), skp(5)),
        trans(del("b"), skp(4)),
        trans(del("ab"), skp(4)),
        "cdef");
  }

  private Random random;

  private String getRandomString(int length) {
    String chars = "abcdefg";
    StringBuilder buf = new StringBuilder();
    for (int i = 0; i < length; i++)
      buf.append(chars.charAt(random.nextInt(chars.length())));
    return buf.toString();
  }

  /**
   * Generate a random transformation that can be applied to the given
   * input.
   */
  private Transform getRandomTransform(String input) {
    TransformBuilder out = new TransformBuilder();
    // First build a list of split points where we'll transform the
    // string.
    TreeSet<Integer> splits = new TreeSet<Integer>();
    while ((splits.size() < input.length() / 6) || (splits.size() % 2 != 0))
      splits.add(random.nextInt(input.length()));
    // Then scan through the points and generate random transformations
    // to apply in those positions.
    Iterator<Integer> splitIter = splits.iterator();
    int cursor = 0;
    while (splitIter.hasNext()) {
      int start = splitIter.next();
      int end = splitIter.next();
      out.skip(start - cursor);
      switch (random.nextInt(2)) {
      case 0:
        out.insert(getRandomString(random.nextInt(3) + 4));
        out.skip(end - start);
        break;
      case 1:
        out.delete(input.substring(start, end));
        break;
      }
      cursor = end;
    }
    out.skip(input.length() - cursor);
    Transform result = out.flush();
    assertEquals(input.length(), result.getInputLength());
    return result;
  }

  @Test
  public void testXformRandomized() {
    this.random = new Random(19124);
    for (int i = 0; i < 1000; i++) {
      String input = getRandomString(100);
      Transform a = getRandomTransform(input);
      Transform b = getRandomTransform(input);
      String aStr = a.call(input);
      String bStr = b.call(input);
      Pair<Transform, Transform> prime = Composer.xform(a, b);
      String aFound = prime.getSecond().call(aStr);
      String bFound = prime.getFirst().call(bStr);
      assertEquals(aFound, bFound);
    }
  }

  @Test
  public void testComposeRandomized() {
    this.random = new Random(42342);
    for (int i = 0; i < 1000; i++) {
      String first = getRandomString(100);
      Transform a = getRandomTransform(first);
      String second = a.call(first);
      Transform b = getRandomTransform(second);
      String expected = b.call(a.call(first));
      Transform ab = Composer.compose(a, b);
      String found = ab.call(first);
      assertEquals(expected, found);
    }
  }

  private static Transform trans(Operation... ops) {
    return new Transform(Arrays.asList(ops));
  }

  private static Operation ins(String str) {
    return new Operation.Insert(str);
  }

  private static Operation del(String str) {
    return new Operation.Delete(str);
  }

  private static Operation skp(int count) {
    return new Operation.Skip(count);
  }

}
