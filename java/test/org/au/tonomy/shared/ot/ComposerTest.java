package org.au.tonomy.shared.ot;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;
import java.util.TreeSet;

import junit.framework.TestCase;

import org.au.tonomy.shared.ot.Operation.Insert;
import org.au.tonomy.shared.util.Pair;
import org.junit.Test;

public class ComposerTest extends TestCase {

  private void checkXform(String original, Transform a, Transform b,
      String expected, Transform aPrime, Transform bPrime) {
    // First check that we produced the right xformed transformations.
    Pair<Transform, Transform> prime = Composer.compose(a, b);
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
  public void testComposeInsert() {
    checkXform(
        "",
        trans(ins("foo")),
        trans(ins("foobar")),
        "foobar",
        trans(skp(6)),
        trans(skp(3), ins("bar")));
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

  public void testComposeSkip() {
    checkXform(
        "foo",
        trans(skp(2), ins("x"), skp(1)),
        trans(skp(1), ins("y"), skp(2)),
        "fyoxo",
        trans(skp(3), ins("x"), skp(1)),
        trans(skp(1), ins("y"), skp(3)));
  }

  public void testComposeDelete() {
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
  }

  public void testComposeDeleteInsert() {
    checkXform(
        "foo",
        trans(ins("bla"), skp(3)),
        trans(del("fo"), skp(1)),
        "blao",
        trans(ins("bla"), skp(1)),
        trans(skp(3), del("fo"), skp(1)));
  }

  @Test
  public void testPrefixOffset() {
    assertEquals(3, Insert.getPrefixOffset("foo", "foobar"));
    assertEquals(3, Insert.getPrefixOffset("foo", "foo"));
    assertEquals(0, Insert.getPrefixOffset("foo", ""));
    assertEquals(0, Insert.getPrefixOffset("foo", "hix"));
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
  private Transform randomTransform(String input) {
    OperationOutputStream out = new OperationOutputStream();
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
        out.insert(getRandomString(6));
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
  public void testRandomized() {
    this.random = new Random(19124);
    String currentStr = getRandomString(100);
    for (int i = 0; i < 200; i++) {
      Transform currentTrans = randomTransform(currentStr);
      String nextStr = currentTrans.call(currentStr);
      assertEquals(currentStr, currentTrans.getInverse().call(nextStr));
      currentStr = nextStr;
    }
    String result = "eacegcfaecbbabedcabagegbccaggdbeeacaceccgdaeeg" +
        "gefagffaeffbgaecacdcebagaaaeaecaagfbfddadabddcdfcagbedfbde" +
        "gdccbecefcddbcegbbgfcceeccaaeagafgbbcecadagdeeggbfgedggecb" +
        "dcdffgegdaecgafgfgcdbggeaafacfegccfafgbdegbaaaeaagfaadeggf" +
        "cggfbfgfccdgaddadgbbgfabcafgdbedbddebafacgcffbgcbgcddfeffb" +
        "dafaabcbcaffgcgffeafafbaafbgfbeffgbbgggfccfgfdeagceabfebea" +
        "afbbaecefdggadgafgefceadfcfcacebbaabeageddebbcgdacdecaagcg" +
        "cafddceabgdbgfgeaafbdfcfgaaffbcdfegfgdageaacdbecddgfcgfcdg" +
        "dabeccaeaagfegbbcgaebacccgf";
    assertEquals(result, currentStr);
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
