package org.au.tonomy.client.presentation;

import java.util.LinkedList;
import java.util.List;

import org.au.tonomy.shared.syntax.IToken;
import org.au.tonomy.shared.syntax.ITokenFilter;
import org.au.tonomy.shared.syntax.ITokenFilter.ITokenListener;
import org.au.tonomy.shared.util.Assert;
import org.au.tonomy.shared.util.Factory;
import org.au.tonomy.shared.util.Pair;

/**
 * A line manager maps between a line-centric view of a text file,
 * including a cursor, and a view of the contents as a sequence of
 * tokens.
 */
public class LineManager<T extends IToken> {

  /**
   * A listener that receives line-based events.
   */
  public interface IListener<T extends IToken> {

    /**
     * The cursor has moved.
     */
    public void onCursorMoved(Cursor cursor);

    /**
     * A new line has been inserted at the given row.
     */
    public void onNewlineAdded(int row);

    /**
     * A sequence of tokens has been inserted at the given row, at
     * the given token index.
     */
    public void onTokensInserted(int row, int tokenIndex, List<T> tokens);

  }

  /**
   * A collection of data associated with an editor cursor.
   */
  public static class Cursor {

    private int row = 0;
    private int column = 0;

    public int getRow() {
      return this.row;
    }

    public int getColumn() {
      return this.column;
    }

  }

  /**
   * The relevant information about a particular line.
   */
  private static class LineInfo<T extends IToken> {

    private final LinkedList<T> tokens = Factory.newLinkedList();

    /**
     * Returns the raw list of tokens in this line.
     */
    public List<T> getTokens() {
      return tokens;
    }

    /**
     * Inserts the given tokens at the given position in this line.
     */
    public void insert(int offset, List<T> tokens) {
      this.tokens.addAll(offset, tokens);
    }

  }

  private final ITokenFilter<T> tokenFilter;
  private final Cursor cursor = new Cursor();
  private final LinkedList<LineInfo<T>> lines = Factory.newLinkedList();
  private IListener<T> listener;

  public LineManager(ITokenFilter<T> tokenFilter) {
    this.tokenFilter = tokenFilter;
    this.tokenFilter.addListener(tokenListener);
  }

  /**
   * Sets the initial source code.
   */
  public void initialize(String source) {
    tokenFilter.append(source);
    getListener().onCursorMoved(cursor);
  }

  /**
   * Attach the given listener and reset it according to the current
   * state of this line manager.
   */
  public void attachListener(IListener<T> listener) {
    Assert.isNull(this.listener);
    this.listener = Assert.notNull(listener);
  }

  private IListener<T> getListener() {
    return Assert.notNull(listener);
  }

  public IEditorListener getEditorListener() {
    return this.editorListener;
  }

  /**
   * The listener that is notified of editor events.
   */
  private final IEditorListener editorListener = new IEditorListener() {

    @Override
    public void moveCursor(int deltaColumn, int deltaRow) {
      cursor.row += deltaRow;
      cursor.column += deltaColumn;
      getListener().onCursorMoved(cursor);
    }

    @Override
    public void deleteBackwards() {

    }

    @Override
    public void insertChar(char key) {

    }

    @Override
    public void insertNewline() {
      addNewlineAndNotify(lines.size());
    }

  };

  /**
   * Inserts a new line and notifies the listener.
   */
  private void addNewlineAndNotify(int row) {
    LineInfo<T> line = new LineInfo<T>();
    lines.add(row, line);
    getListener().onNewlineAdded(row);
  }

  private void insertTokensAndNotify(int row, int tokenIndex, List<T> tokens) {
    LineInfo<T> lineInfo = lines.get(row);
    lineInfo.insert(tokenIndex, tokens);
    getListener().onTokensInserted(row, tokenIndex, tokens);
  }

  /**
   * Returns the index of the line that contains the token with the
   * given index and the index of the token within the line.
   */
  private Pair<Integer, Integer> getLineForOffset(int tokenOffset) {
    int currentOffset = 0;
    int lineIndex = 0;
    for (LineInfo<T> line : lines) {
      int nextOffset = currentOffset + line.getTokens().size();
      if (currentOffset <= tokenOffset && tokenOffset < nextOffset)
        return Pair.of(lineIndex, tokenOffset - currentOffset);
      lineIndex++;
    }
    Assert.equals(tokenOffset, currentOffset);
    return Pair.of(lines.size(), 0);
  }

  private final ITokenListener<T> tokenListener = new ITokenListener<T>() {

    @Override
    public void onInsert(int tokenOffset, List<T> inserted) {
      Pair<Integer, Integer> position = getLineForOffset(tokenOffset);
      int lineIndex = position.getFirst();
      int tokenIndex = position.getSecond();
      if (lineIndex == lines.size())
        addNewlineAndNotify(lineIndex);
      insertTokensAndNotify(lineIndex, tokenIndex, inserted);
    }

    @Override
    public void onRemove(int offset, List<T> removed) {
      // TODO Auto-generated method stub

    }

    @Override
    public void onReplace(int offset, List<T> removed, List<T> inserted) {
      // TODO Auto-generated method stub

    }

  };



}
