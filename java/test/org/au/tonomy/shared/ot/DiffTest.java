package org.au.tonomy.shared.ot;

import static org.au.tonomy.testing.TestUtils.del;
import static org.au.tonomy.testing.TestUtils.ins;
import static org.au.tonomy.testing.TestUtils.skp;
import static org.au.tonomy.testing.TestUtils.trans;
import junit.framework.TestCase;

import org.junit.Test;

public class DiffTest extends TestCase {

  private void checkDiff(String before, String after, Transform expected) {
    Transform trans = Diff.diff(before, after);
    assertEquals(after, trans.call(before));
    assertEquals(expected, trans);
  }

  @Test
  public void testSimpleDiff() {
    checkDiff("abc", "", trans(del("abc")));
    checkDiff("", "abc", trans(ins("abc")));
    checkDiff("abc", "xyz", trans(del("abc"), ins("xyz")));
    checkDiff("abc", "xbc", trans(del("a"), ins("x"), skp(2)));
    checkDiff("1234abcdef", "1234xyz", trans(skp(4), del("abcdef"), ins("xyz")));
    checkDiff("1234", "1234xyz", trans(skp(4), ins("xyz")));
    checkDiff("abcdef1234", "xyz1234", trans(del("abcdef"), ins("xyz"), skp(4)));
    checkDiff("1234", "xyz1234", trans(ins("xyz"), skp(4)));
    checkDiff("", "abcd", trans(ins("abcd")));
    checkDiff("abc", "abcd", trans(skp(3), ins("d")));
    checkDiff("123456", "abcd", trans(del("123456"), ins("abcd")));
    checkDiff("123456xxx", "xxxabcd", trans(del("123456"), skp(3), ins("abcd")));
    checkDiff("jumps over the lazy dog", "jumped over a lazy dog",
        trans(skp(4), del("s"), ins("ed"), skp(6), del("the"), ins("a"),
            skp(9)));
  }

}
