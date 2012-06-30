package org.au.tonomy.client.widget;

import org.au.tonomy.client.widget.CodeEditorWidget.IKeyEvent;

import com.google.gwt.event.dom.client.KeyCodes;


/**
 * The presenter for the code editor. The presenter doesn't know what
 * text is in the buffer, it simply does event plumbing and leaves it
 * to the manager to keep track of the buffer contents.
 */
public class CodeEditorPresenter implements CodeEditorWidget.IListener {

  private final CodeEditorWidget editor;
  private final SourceManager manager = new SourceManager();

  public CodeEditorPresenter(CodeEditorWidget editor) {
    this.editor = editor;
    editor.attachListener(this);
    manager.attachListener(editor);
    manager.resetListener();
  }

  @Override
  public void onKeyPress(char key) {
    switch (key) {
    case '\r': case '\n':
      manager.insertNewline();
      break;
    default:
      manager.appendChar((char) key);
      break;
    }
  }

  @Override
  public void onKeyDown(IKeyEvent event) {
    switch (event.getKeyCode()) {
    case KeyCodes.KEY_BACKSPACE:
      manager.deleteBackwards();
      break;
    case KeyCodes.KEY_UP:
      manager.moveCursor(0, -1);
      break;
    case KeyCodes.KEY_DOWN:
      manager.moveCursor(0, 1);
      break;
    case KeyCodes.KEY_LEFT:
      manager.moveCursor(-1, 0);
      break;
    case KeyCodes.KEY_RIGHT:
      manager.moveCursor(1, 0);
      break;
    }
  }

}
