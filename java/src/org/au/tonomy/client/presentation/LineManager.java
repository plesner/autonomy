package org.au.tonomy.client.presentation;

import java.util.LinkedList;
import java.util.List;

import org.au.tonomy.shared.syntax.IToken;
import org.au.tonomy.shared.syntax.IToken.Type;
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
    public void onCursorMoved(int row, int column);

    /**
     * A new empty line has been inserted at the given row.
     */
    public void onNewLine(int row);

    /**
     * A sequence of tokens has been inserted at the given row, at
     * the given token index.
     */
    public void onTokensInserted(int row, int tokenIndex, List<T> tokens);

    /**
     * A sequence of tokens has been removed at the given point.
     */
    public void onTokensRemoved(int row, int tokenIndex, List<T> tokens);

  }

  /**
   * A collection of data associated with an editor cursor.
   */
  private static class Cursor {

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
    private int charCountCache = -1;

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
      charCountCache = -1;
    }

    /**
     * Removes the given tokens from this manager.
     */
    public void remove(int offset, List<T> tokens) {
      List<T> toRemove = this.tokens.subList(offset, offset + tokens.size());
      Assert.equals(toRemove, tokens);
      toRemove.clear();
      charCountCache = -1;
    }

    /**
     * Returns the number of characters in this line.
     */
    public int getCharCount() {
      if (charCountCache == -1) {
        charCountCache = 0;
        for (T token : tokens)
          charCountCache += token.getValue().length();
      }
      return charCountCache;
    }

  }

  private final ITokenFilter<T> tokenFilter;
  private final Cursor cursor = new Cursor();
  private final LinkedList<LineInfo<T>> lines = Factory.newLinkedList();
  private IListener<T> listener;

  public LineManager(ITokenFilter<T> tokenFilter) {
    this.tokenFilter = tokenFilter;
    this.tokenFilter.addListener(tokenListener);
    this.lines.add(new LineInfo<T>());
  }

  public int getLineCount() {
    return lines.size();
  }

  /**
   * Sets the initial source code.
   */
  public void initialize(String source) {
    tokenFilter.append(source);
    getListener().onCursorMoved(cursor.getRow(), cursor.getColumn());
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
    public void moveCursor(int deltaRow, int deltaColumn) {
      setCursor(cursor.row + deltaRow, cursor.column + deltaColumn);
    }

    private void setCursor(int newRow, int newColumn) {
      LineInfo<T> currentRow = lines.get(cursor.row);
      if (newColumn > currentRow.getCharCount()) {
        newColumn = currentRow.getCharCount();
      } else if (newColumn < 0) {
        newColumn = 0;
      }
      cursor.column = newColumn;
      cursor.row += newRow;
      getListener().onCursorMoved(cursor.getRow(), cursor.getColumn());
    }

    @Override
    public void deleteBackwards() {

    }

    @Override
    public void insertChar(char key) {
      int charOffset = getLineCharOffset(cursor.row);
      tokenFilter.insert(charOffset + cursor.column, Character.toString(key));
    }

  };

  /**
   * Returns the character offset of the beginning of the given row.
   * @param row
   * @return
   */
  private int getLineCharOffset(int row) {
    int offset = 0;
    for (int i = 0; i < row; i++)
      offset += lines.get(i).getCharCount();
    return offset;
  }

  /**
   * Inserts a new line and notifies the listener.
   */
  private void addNewlineAndNotify(int row) {
    LineInfo<T> line = new LineInfo<T>();
    lines.add(row, line);
    getListener().onNewLine(row);
  }

  private void insertTokensAndNotify(int row, int tokenIndex, List<T> tokens) {
    LineInfo<T> lineInfo = lines.get(row);
    lineInfo.insert(tokenIndex, tokens);
    getListener().onTokensInserted(row, tokenIndex, tokens);
  }

  private void removeTokensAndNotify(int row, int column, List<T> tokens) {
    LineInfo<T> lineInfo = lines.get(row);
    lineInfo.remove(column, tokens);
    getListener().onTokensRemoved(row, column, tokens);
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
      if (currentOffset <= tokenOffset && tokenOffset <= nextOffset)
        return Pair.of(lineIndex, tokenOffset - currentOffset);
      lineIndex++;
      currentOffset = nextOffset;
    }
    Assert.equals(tokenOffset, currentOffset);
    return Pair.of(lines.size(), 0);
  }

  /**
   * Returns the index of the next newline token in the given list.
   */
  private int getNextNewline(List<T> tokens) {
    for (int i = 0; i < tokens.size(); i++) {
      if (tokens.get(i).is(Type.NEWLINE))
        return i;
    }
    return -1;
  }

  private List<List<T>> splitLines(List<T> tokens) {
    List<List<T>> lines = Factory.newArrayList();
    List<T> current = tokens;
    while (!current.isEmpty()) {
      int newline = getNextNewline(current);
      if (newline == -1) {
        lines.add(current);
        break;
      } else {
        List<T> head = current.subList(0, newline + 1);
        List<T> tail = current.subList(newline + 1, current.size());
        lines.add(head);
        current = tail;
      }
    }
    return lines;
  }

  private final ITokenListener<T> tokenListener = new ITokenListener<T>() {

    @Override
    public void onInsert(int tokenOffset, List<T> inserted) {
      List<List<T>> lines = splitLines(inserted);
      // First locate the current line where we'll insert these tokens.
      // The first line is special because it's the only one where we'll
      // insert in the middle of a line.
      Pair<Integer, Integer> position = getLineForOffset(tokenOffset);
      int lineIndex = position.getFirst();
      int tokenIndex = position.getSecond();
      // Then loop around, adding lines one at a time.
      for (int i = 0; i < lines.size(); i++) {
        if (i > 0)
          addNewlineAndNotify(lineIndex);
        insertTokensAndNotify(lineIndex, tokenIndex, lines.get(i));
        lineIndex++;
        tokenIndex = 0;
      }
    }

    @Override
    public void onRemove(int tokenOffset, List<T> removed) {
      List<List<T>> lines = splitLines(removed);
      Pair<Integer, Integer> position = getLineForOffset(tokenOffset);
      int lineIndex = position.getFirst();
      int tokenIndex = position.getSecond();
      for (int i = 0; i < lines.size(); i++) {
        removeTokensAndNotify(lineIndex, tokenIndex, lines.get(i));
        tokenIndex = 0;
      }
    }

    @Override
    public void onReplace(int tokenOffset, List<T> removed, List<T> inserted) {
      onRemove(tokenOffset, removed);
      onInsert(tokenOffset, inserted);
    }

  };



}
