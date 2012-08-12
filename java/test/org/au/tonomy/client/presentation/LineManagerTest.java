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

    }

  }

  @Test
  public void testCursorMoves() {
    EventChecker checker = new EventChecker();
    LineManager<Token> manager = new LineManager<Token>(new DumbTokenFilter<Token>(Token.getFactory()));
    manager.attachListener(checker);

    checker.getRecorder().onNewLine(0);
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
  }

}
