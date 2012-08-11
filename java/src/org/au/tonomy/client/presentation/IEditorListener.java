package org.au.tonomy.client.presentation;
/**
 * The interface of a component that listens to the operations being
 * performed in an editor.
 */
public interface IEditorListener {

  /**
   * Notification that the cursor has been moved.
   */
  public void moveCursor(int deltaRow, int deltaColumn);

  public void deleteBackwards();

  public void insertChar(char key);

  public void insertNewline();

}
