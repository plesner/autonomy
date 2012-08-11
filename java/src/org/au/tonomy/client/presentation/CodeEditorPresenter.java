package org.au.tonomy.client.presentation;

import org.au.tonomy.client.widget.EditorWidget;
import org.au.tonomy.client.widget.EditorWidget.IKeyEvent;
import org.au.tonomy.client.widget.EditorToken;
import org.au.tonomy.shared.syntax.DumbTokenFilter;

import com.google.gwt.event.dom.client.KeyCodes;


/**
 * The presenter for the code editor. The presenter doesn't know what
 * text is in the buffer, it simply does event plumbing and leaves it
 * to the manager to keep track of the buffer contents.
 */
public class CodeEditorPresenter implements EditorWidget.IListener {

  private final LineManager<EditorToken> manager;
  private final IEditorListener listener;

  public CodeEditorPresenter(EditorWidget editor) {
    this.manager = new LineManager<EditorToken>(
        new DumbTokenFilter<EditorToken>(
            EditorToken.getWidgetFactory()));
    this.listener = manager.getEditorListener();
    editor.attachListener(this);
    manager.attachListener(editor.getLineListener());
  }

  public void initialize(String source) {
    this.manager.initialize(source);
  }

  @Override
  public void onKeyPress(char key) {
    switch (key) {
    case '\r': case '\n':
      listener.insertNewline();
      break;
    default:
      listener.insertChar((char) key);
      break;
    }
  }

  @Override
  public void onKeyDown(IKeyEvent event) {
    switch (event.getKeyCode()) {
    case KeyCodes.KEY_BACKSPACE:
      listener.deleteBackwards();
      break;
    case KeyCodes.KEY_UP:
      listener.moveCursor(0, -1);
      break;
    case KeyCodes.KEY_DOWN:
      listener.moveCursor(0, 1);
      break;
    case KeyCodes.KEY_LEFT:
      listener.moveCursor(-1, 0);
      break;
    case KeyCodes.KEY_RIGHT:
      listener.moveCursor(1, 0);
      break;
    }
  }

}
