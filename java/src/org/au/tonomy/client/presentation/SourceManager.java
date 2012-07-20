package org.au.tonomy.client.presentation;

import java.util.LinkedList;
import java.util.List;

import org.au.tonomy.shared.syntax.Ast;
import org.au.tonomy.shared.util.Assert;

public class SourceManager {

  public interface IListener {

    public void resetContent(List<String> lines);

    /**
     * Line 'row' was changed to be the specified value.
     */
    public void onLineChanged(int row, String line);

    /**
     * A new line was appended at the end of the buffer.
     */
    public void onLineAppended(String value);

    /**
     * The cursor was move to a new position.
     */
    public void setCursor(int row, int column);

  }

  private int cursorRow = 0;
  private int cursorColumn = 0;
  private final LinkedList<String> rows = new LinkedList<String>();
  private IListener listener;

  public SourceManager() {
    rows.add("");
  }

  public void attachListener(IListener listener) {
    Assert.that(this.listener == null);
    this.listener = listener;
  }

  private IListener getListener() {
    return Assert.notNull(listener);
  }

  /**
   * Appends a character at the current position.
   */
  public void appendChar(char c) {
    String newLine = rows.get(cursorRow);
    newLine += c;
    doUpdateLine(cursorRow, newLine);
    doUpdateCursor(cursorRow, cursorColumn + 1);
  }

  public void deleteBackwards() {
    if (cursorColumn == 0)
      return;
    String oldLine = rows.get(cursorRow);
    String start = oldLine.substring(0, cursorColumn - 1);
    String end = oldLine.substring(cursorColumn);
    String newLine = start + end;
    doUpdateLine(cursorRow, newLine);
    doUpdateCursor(cursorRow, cursorColumn - 1);
  }

  private void doUpdateLine(int row, String line) {
    rows.set(row, line);
    getListener().onLineChanged(row, line);
  }

  private void doUpdateCursor(int row, int column) {
    this.cursorRow = row;
    this.cursorColumn = column;
    getListener().setCursor(row, column);
  }

  public void insertNewline() {
    if (cursorRow == rows.size() - 1) {
      doAppendLine("");
    } else {
      // ...
    }
    doUpdateCursor(cursorRow + 1, 0);
  }

  private void doAppendLine(String value) {
    rows.add(value);
    getListener().onLineAppended(value);
  }

  /**
   * Uses listener events to reset the listener to the current state
   * of this source manager.
   */
  public void resetListener() {
    getListener().resetContent(rows);
    getListener().setCursor(cursorRow, cursorColumn);
  }

  public void setSource(Ast ast) {

  }

  public void moveCursor(int columnDelta, int rowDelta) {
    cursorRow = Math.min(Math.max(cursorRow + rowDelta, 0), rows.size() - 1);
    cursorColumn = Math.min(Math.max(cursorColumn + columnDelta, 0), rows.get(cursorRow).length());
    getListener().setCursor(cursorRow, cursorColumn);
  }

}
