package org.au.tonomy.shared.syntax;

import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import org.au.tonomy.shared.syntax.ITokenFilter.ITokenListener;
import org.au.tonomy.shared.util.Factory;
import org.au.tonomy.testing.TestUtils;
import org.junit.Test;

public class DumbTokenFilterTest extends TestCase {

  private static List<Token> tokens(String... values) {
    List<Token> tokens = Factory.newArrayList();
    for (String value : values) {
      if (TestUtils.isSpace(value)) {
        tokens.add(Token.getFactory().newEther(value));
      } else {
        tokens.add(Token.getFactory().newWord(value));
      }
    }
    return tokens;
  }

  @Test
  public void testFirstDifference() {
    assertEquals(2, DumbTokenFilter.findFirstDifference(
        tokens("foo", "bar", "zoom"),
        tokens("foo", "bar", "baz")));
    assertEquals(2, DumbTokenFilter.findFirstDifference(
        tokens("foo", "bar", "zoom"),
        tokens("foo", "bar")));
    assertEquals(2, DumbTokenFilter.findFirstDifference(
        tokens("foo", "bar"),
        tokens("foo", "bar", "baz")));
    assertEquals(0, DumbTokenFilter.findFirstDifference(
        tokens("doof", "bar"),
        tokens("foo", "bar")));
    assertEquals(2, DumbTokenFilter.findFirstDifference(
        tokens("doof", "bar"),
        tokens("doof", "bar")));
  }

  @Test
  public void testLastDifference() {
    assertEquals(2, DumbTokenFilter.findLastDifference(
        tokens("zoom", "bar", "foo"),
        tokens("baz", "bar", "foo"), 0));
    assertEquals(2, DumbTokenFilter.findLastDifference(
        tokens("zoom", "bar", "foo"),
        tokens("bar", "foo"), 0));
    assertEquals(2, DumbTokenFilter.findLastDifference(
        tokens("bar", "foo"),
        tokens("baz", "bar", "foo"), 0));
    assertEquals(0, DumbTokenFilter.findLastDifference(
        tokens("bar", "doof"),
        tokens("bar", "foo"), 1));
    assertEquals(0, DumbTokenFilter.findLastDifference(
        tokens("doof", "bar"),
        tokens("doof", "bar"), 2));
  }

  /**
   * Listener that fails all events.
   */
  private class FailingListener implements ITokenListener<Token> {

    @Override
    public void onInsert(int offset, List<Token> inserted) {
      fail();
    }

    @Override
    public void onRemove(int offset, List<Token> removed) {
      fail();
    }

    @Override
    public void onReplace(int offset, List<Token> removed, List<Token> inserted) {
      fail();
    }

  }

  /**
   * A utility class the can be loaded with event expectations and will
   * check any events fired on it against those expectations.
   */
  private class EventChecker implements ITokenListener<Token> {

    /**
     * The expected events, in the form of listeners that will throw
     * if they're called in the wrong way. New are added at the end.
     */
    private final LinkedList<ITokenListener<Token>> expected = Factory.newLinkedList();

    public void expectInsert(final int expectedOffset, final String... expectedTokens) {
      expected.addLast(new FailingListener() {
        @Override
        public void onInsert(int offset, List<Token> inserted) {
          assertEquals(expectedOffset, offset);
          assertEquals(tokens(expectedTokens), inserted);
        }
      });
    }

    @Override
    public void onInsert(int offset, List<Token> inserted) {
      getNextEvent().onInsert(offset, inserted);
    }

    public void expectRemove(final int expectedOffset, final String... expectedTokens) {
      expected.addLast(new FailingListener() {
        @Override
        public void onRemove(int offset, List<Token> removed) {
          assertEquals(expectedOffset, offset);
          assertEquals(tokens(expectedTokens), removed);
        }
      });
    }

    @Override
    public void onRemove(int offset, List<Token> removed) {
      getNextEvent().onRemove(offset, removed);
    }

    public void expectReplace(final int expectedOffset,
        final List<Token> expectedRemoved, final List<Token> expectedInserted) {
      expected.addLast(new FailingListener() {
        @Override
        public void onReplace(int offset, List<Token> removed,
            List<Token> inserted) {
          assertEquals(expectedOffset, offset);
          assertEquals(expectedRemoved, removed);
          assertEquals(expectedInserted, inserted);
        }
      });
    }

    @Override
    public void onReplace(int offset, List<Token> removed, List<Token> inserted) {
      getNextEvent().onReplace(offset, removed, inserted);
    }

    private ITokenListener<Token> getNextEvent() {
      assertTrue(expectsMoreEvents());
      return expected.removeFirst();
    }

    public boolean expectsMoreEvents() {
      return !expected.isEmpty();
    }

  }

  @Test
  public void testInsert() {
    EventChecker checker = new EventChecker();
    DumbTokenFilter<Token> filter = new DumbTokenFilter<Token>(Token.getFactory());
    filter.addListener(checker);

    checker.expectInsert(0, "foo", " ", "baz");
    filter.append("foo baz");
    assertEquals("foo baz", filter.getSource());

    checker.expectInsert(2, "bar", " ");
    filter.insert(4, "bar ");
    assertEquals("foo bar baz", filter.getSource());

    checker.expectRemove(2, "bar", " ");
    filter.delete(4, 4);
    assertEquals("foo baz", filter.getSource());

    checker.expectReplace(2, tokens("baz"), tokens("bar"));
    filter.replace(4, 3, "bar");
    assertEquals("foo bar", filter.getSource());

    // Do the same replacement again to see that it has no effect.
    filter.replace(4, 3, "bar");
    assertEquals("foo bar", filter.getSource());

    // Check that "replacing" the empty string works just like inserting.
    checker.expectInsert(2, "blah", " ");
    filter.replace(4, 0, "blah ");
    assertEquals("foo blah bar", filter.getSource());

    assertFalse(checker.expectsMoreEvents());
  }

}
