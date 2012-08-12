package org.au.tonomy.client.presentation;

import static org.au.tonomy.testing.TestUtils.tokens;

import java.util.List;

import junit.framework.TestCase;

import org.au.tonomy.client.presentation.LineManager.IListener;
import org.au.tonomy.shared.syntax.DumbTokenFilter;
import org.au.tonomy.shared.syntax.Token;
import org.au.tonomy.testing.AbstractEventChecker;
import org.junit.Test;

public class LineManagerTest extends TestCase {

  /**
   * A line manager listener that fails all calls.
   */
  private static class AbstractListener implements IListener<Token> {

    @Override
    public void onCursorMoved(int row, int column) {
      fail();
    }

    @Override
    public void onNewLine(int row) {
      fail();
    }

    @Override
    public void onTokensInserted(int row, int tokenIndex, List<Token> tokens) {
      fail();
    }

    @Override
    public void onTokensRemoved(int row, int column, List<Token> tokens) {
      fail();
    }

  }

  private static class EventChecker extends AbstractEventChecker<IListener<Token>> implements IListener<Token> {

    @Override
    public void onCursorMoved(int row, int column) {
      getNextExpected().onCursorMoved(row, column);
    }

    @Override
    public void onNewLine(int row) {
      getNextExpected().onNewLine(row);
    }

    @Override
    public void onTokensInserted(int row, int tokenIndex, List<Token> tokens) {
      getNextExpected().onTokensInserted(row, tokenIndex, tokens);
    }

    @Override
    public void onTokensRemoved(int row, int column, List<Token> tokens) {
      getNextExpected().onTokensRemoved(row, column, tokens);
    }

    @Override
    protected IListener<Token> newRecorder() {
      return new Recorder();
    }

    private class Recorder implements IListener<Token> {

      @Override
      public void onCursorMoved(final int eRow, final int eColumn) {
        addExpectation(new AbstractListener() {
          @Override
          public void onCursorMoved(int row, int column) {
            assertEquals(eRow, row);
            assertEquals(eColumn, column);
          }
        });
      }

      @Override
      public void onNewLine(final int eRow) {
        addExpectation(new AbstractListener() {
          @Override
          public void onNewLine(int row) {
            assertEquals(eRow, row);
          }
        });
      }

      @Override
      public void onTokensInserted(final int eRow, final int eTokenIndex,
          final List<Token> eTokens) {
        addExpectation(new AbstractListener() {
          @Override
          public void onTokensInserted(int row, int tokenIndex,
              List<Token> tokens) {
            assertEquals(eRow, row);
            assertEquals(eTokenIndex, tokenIndex);
            assertEquals(eTokens, tokens);
          }
        });
      }

      @Override
      public void onTokensRemoved(final int eRow, final int eColumn,
          final List<Token> eTokens) {
        addExpectation(new AbstractListener() {
          @Override
          public void onTokensRemoved(int row, int column, List<Token> tokens) {
            assertEquals(eRow, row);
            assertEquals(eColumn, column);
            assertEquals(eTokens, tokens);
          }
        });
      }

    }

  }

  private LineManager<Token> newLineManager(EventChecker checker) {
    LineManager<Token> result = new LineManager<Token>(new DumbTokenFilter<Token>(
        Token.getFactory()));
    result.attachListener(checker);
    return result;
  }

  @Test
  public void testColumnMoves() {
    EventChecker checker = new EventChecker();
    LineManager<Token> manager = newLineManager(checker);

    checker.getRecorder().onTokensInserted(0, 0, tokens("foo", " ", "bar", " ", "baz"));
    checker.getRecorder().onCursorMoved(0, 0);
    manager.initialize("foo bar baz");
    assertEquals(1, manager.getLineCount());

    checker.getRecorder().onCursorMoved(0, 1);
    manager.getEditorListener().moveCursor(0, 1);
    checker.getRecorder().onCursorMoved(0, 2);
    manager.getEditorListener().moveCursor(0, 1);
    checker.getRecorder().onCursorMoved(0, 11);
    manager.getEditorListener().moveCursor(0, 100);
    checker.getRecorder().onCursorMoved(0, 0);
    manager.getEditorListener().moveCursor(0, -100);
  }

  @Test
  public void testInitialNewlineHandling() {
    EventChecker checker = new EventChecker();
    LineManager<Token> manager = newLineManager(checker);

    checker.getRecorder().onTokensInserted(0, 0, tokens("foo", " ", "bar", "\n"));
    checker.getRecorder().onNewLine(1);
    checker.getRecorder().onTokensInserted(1, 0, tokens("baz", " ", "quux", "\n"));
    checker.getRecorder().onNewLine(2);
    checker.getRecorder().onTokensInserted(2, 0, tokens("corge"));
    checker.getRecorder().onCursorMoved(0, 0);
    manager.initialize("foo bar\nbaz quux\ncorge");
    assertEquals(3, manager.getLineCount());
  }

  @Test
  public void testInserting() {
    EventChecker checker = new EventChecker();
    LineManager<Token> manager = newLineManager(checker);

    checker.getRecorder().onTokensInserted(0, 0, tokens("foo", " ", "bar", " ", "baz"));
    checker.getRecorder().onCursorMoved(0, 0);
    manager.initialize("foo bar baz");

    checker.getRecorder().onCursorMoved(0, 5);
    manager.getEditorListener().moveCursor(0, 5);

    checker.getRecorder().onTokensRemoved(0, 2, tokens("bar"));
    checker.getRecorder().onTokensInserted(0, 2, tokens("baar"));
    manager.getEditorListener().insertChar('a');

    checker.getRecorder().onTokensRemoved(0, 2, tokens("baar"));
    checker.getRecorder().onTokensInserted(0, 2, tokens("b", " ", "aar"));
    manager.getEditorListener().insertChar(' ');
  }

}
