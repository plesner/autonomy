package org.au.tonomy.client.widget;

import java.util.LinkedList;
import java.util.List;

import org.au.tonomy.client.presentation.LineManager;
import org.au.tonomy.shared.util.Assert;
import org.au.tonomy.shared.util.Factory;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;
/**
 * HTML based source code editor.
 */
public class EditorWidget extends Composite {

  private static IEditorWidgetUiBinder BINDER = GWT .create(IEditorWidgetUiBinder.class);
  interface IEditorWidgetUiBinder extends UiBinder<Widget, EditorWidget> { }

  /**
   * A wrapper around native key events.
   */
  public interface IKeyEvent {

    public int getKeyCode();

  }

  public interface IListener {

    public void onKeyDown(IKeyEvent event);

    public void onKeyPress(char key);

  }

  @UiField FlowPanel root;
  @UiField FlowPanel display;
  @UiField TextArea overlay;
  private IListener listener = null;

  private final SpanElement cursor;
  private final LinkedList<EditorLineWidget> lines = Factory.newLinkedList();

  public EditorWidget() {
    initWidget(BINDER.createAndBindUi(this));
    configureEvents();
    cursor = Document.get().createSpanElement();
    cursor.setClassName(RESOURCES.css().cursor());
    root.getElement().appendChild(cursor);
    overlay.getElement().setAttribute("contentEditable", "true");
    lineListener.onNewLine(0);
  }

  public void attachListener(IListener listener) {
    Assert.that(this.listener == null);
    this.listener = listener;
  }

  private IListener getListener() {
    return Assert.notNull(listener);
  }

  private void configureEvents() {
    overlay.addKeyDownHandler(new KeyDownHandler() {
      @Override
      public void onKeyDown(KeyDownEvent event) {
        dispatchKeyEvent(event);
      }
    });
    overlay.addKeyPressHandler(new KeyPressHandler() {
      @Override
      public void onKeyPress(KeyPressEvent event) {
        getListener().onKeyPress(event.getCharCode());
      }
    });
  }

  private void dispatchKeyEvent(final KeyDownEvent rawEvent) {
    IKeyEvent event = new IKeyEvent() {
      @Override
      public int getKeyCode() {
        return rawEvent.getNativeKeyCode();
      }
    };
    getListener().onKeyDown(event);
  }

  public LineManager.IListener<EditorToken> getLineListener() {
    return lineListener;
  }

  private final LineManager.IListener<EditorToken> lineListener = new LineManager.IListener<EditorToken>() {

    @Override
    public void onCursorMoved(int row, int column) {
      Assert.that(row < lines.size());
      EditorLineWidget line = lines.get(row);
      cursor.getStyle().setTop(line.getContentTop(), Unit.PX);
      int charWidth = line.getCharacterWidth();
      cursor.getStyle().setLeft(charWidth * column, Unit.PX);
    }

    @Override
    public void onNewLine(int row) {
      EditorLineWidget line = new EditorLineWidget();
      lines.add(row, line);
      display.insert(line, row);
    }

    @Override
    public void onTokensInserted(int row, int tokenIndex,
        List<EditorToken> tokens) {
      Assert.that(row < lines.size());
      EditorLineWidget line = lines.get(row);
      line.insert(tokenIndex, tokens);
    }

    @Override
    public void onTokensRemoved(int row, int tokenIndex, List<EditorToken> tokens) {
      Assert.that(row < lines.size());
      EditorLineWidget line = lines.get(row);
      line.remove(tokenIndex, tokens);
    }

  };

  /**
   * Bindings for the world widget css.
   */
  public interface Css extends CssResource {

    public String root();

    public String display();

    public String cursor();

    public String overlay();

    public String source();

  }

  /**
   * World widget resource bundle.
   */
  public interface Resources extends ClientBundle {

    @Source("EditorWidget.css")
    public Css css();

  }

  public static final Resources RESOURCES = GWT.create(Resources.class);
  static { RESOURCES.css().ensureInjected(); }

}
