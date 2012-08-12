package org.au.tonomy.shared.syntax;

import static org.au.tonomy.testing.TestUtils.tokens;

import java.util.List;

import junit.framework.TestCase;

import org.au.tonomy.shared.syntax.ITokenFilter.ITokenListener;
import org.au.tonomy.testing.AbstractEventChecker;
import org.junit.Test;

public class DumbTokenFilterTest extends TestCase {

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
  private class AbstractListener implements ITokenListener<Token> {

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
  private class EventChecker extends AbstractEventChecker<ITokenListener<Token>>
      implements ITokenListener<Token> {

    @Override
    public void onInsert(int offset, List<Token> inserted) {
      getNextExpected().onInsert(offset, inserted);
    }

    @Override
    public void onRemove(int offset, List<Token> removed) {
      getNextExpected().onRemove(offset, removed);
    }

    @Override
    public void onReplace(int offset, List<Token> removed, List<Token> inserted) {
      getNextExpected().onReplace(offset, removed, inserted);
    }

    @Override
    protected ITokenListener<Token> newRecorder() {
      return new Recorder();
    }

    private class Recorder implements ITokenListener<Token> {

      @Override
      public void onInsert(final int eOffset, final List<Token> eInserted) {
        addExpectation(new AbstractListener() {
          @Override
          public void onInsert(int offset, List<Token> inserted) {
            assertEquals(eOffset, offset);
            assertEquals(eInserted, inserted);
          }
        });
      }

      @Override
      public void onRemove(final int eOffset, final List<Token> eRemoved) {
        addExpectation(new AbstractListener() {
          @Override
          public void onRemove(int offset, List<Token> removed) {
            assertEquals(eOffset, offset);
            assertEquals(eRemoved, removed);
          }
        });
      }

      @Override
      public void onReplace(final int eOffset, final List<Token> eRemoved,
          final List<Token> eInserted) {
        addExpectation(new AbstractListener() {
          @Override
          public void onReplace(int offset, List<Token> removed,
              List<Token> inserted) {
            assertEquals(eOffset, offset);
            assertEquals(eRemoved, removed);
            assertEquals(eInserted, inserted);
          }
        });
      }

    }

  }

  @Test
  public void testInsert() {
    EventChecker checker = new EventChecker();
    DumbTokenFilter<Token> filter = new DumbTokenFilter<Token>(Token.getFactory());
    filter.addListener(checker);

    checker.getRecorder().onInsert(0, tokens("foo", " ", "baz"));
    filter.append("foo baz");
    assertEquals("foo baz", filter.getSource());

    checker.getRecorder().onInsert(2, tokens("bar", " "));
    filter.insert(4, "bar ");
    assertEquals("foo bar baz", filter.getSource());

    checker.getRecorder().onRemove(2, tokens("bar", " "));
    filter.delete(4, 4);
    assertEquals("foo baz", filter.getSource());

    checker.getRecorder().onReplace(2, tokens("baz"), tokens("bar"));
    filter.replace(4, 3, "bar");
    assertEquals("foo bar", filter.getSource());

    // Do the same replacement again to see that it has no effect.
    filter.replace(4, 3, "bar");
    assertEquals("foo bar", filter.getSource());

    // Check that "replacing" the empty string works just like inserting.
    checker.getRecorder().onInsert(2, tokens("blah", " "));
    filter.replace(4, 0, "blah ");
    assertEquals("foo blah bar", filter.getSource());

    assertFalse(checker.expectsMoreEvents());
  }

}
