package org.au.tonomy.client.widget;

import java.util.LinkedList;
import java.util.List;

import org.au.tonomy.client.presentation.SourceManager;
import org.au.tonomy.shared.syntax.Token;
import org.au.tonomy.shared.syntax.Tokenizer;
import org.au.tonomy.shared.util.Assert;

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
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;
/**
 * HTML based source code editor.
 */
public class CodeEditorWidget extends Composite implements SourceManager.IListener {

  private static ICodeEditorWidgetUiBinder BINDER = GWT .create(ICodeEditorWidgetUiBinder.class);
  interface ICodeEditorWidgetUiBinder extends UiBinder<Widget, CodeEditorWidget> { }

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

  @UiField FocusPanel display;
  @UiField TextArea overlay;
  private IListener listener = null;

  private final LinkedList<SpanElement> lineSpans = new LinkedList<SpanElement>();
  private final SpanElement cursor;

  public CodeEditorWidget() {
    initWidget(BINDER.createAndBindUi(this));
    configureEvents();
    cursor = Document.get().createSpanElement();
    cursor.setClassName(RESOURCES.css().cursor());
    display.getElement().appendChild(cursor);
    overlay.getElement().setAttribute("contentEditable", "true");
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

  @Override
  public void resetContent(List<String> lines) {
    display.clear();
    for (String line : lines)
      appendLine(line);
  }

  @Override
  public void resetCursor(int row, int column) {
    Assert.that(row < lineSpans.size());
    lineSpans.get(row);
  }

  @Override
  public void onLineChanged(int row, String line) {
    Assert.that(row < lineSpans.size());
    SpanElement span = lineSpans.get(row);
    span.setInnerText("");
    List<Token> tokens = Tokenizer.tokenize(line);
    for (Token token : tokens) {
      SpanElement tokenSpan = Document.get().createSpanElement();
      tokenSpan.setInnerText(token.getValue());
      tokenSpan.addClassName(token.getCategory());
      span.appendChild(tokenSpan);
    }
  }

  @Override
  public void onLineAppended(String value) {
    appendLine(value);
  }

  private void appendLine(String value) {
    SpanElement span = Document.get().createSpanElement();
    span.addClassName(RESOURCES.css().lineSpan());
    lineSpans.add(span);
    display.getElement().appendChild(span);
    onLineChanged(lineSpans.size() - 1, value);
  }

  @Override
  public void onCursorMoved(int row, int column) {
    SpanElement lineSpan = lineSpans.get(row);
    cursor.getStyle().setTop(lineSpan.getAbsoluteTop(), Unit.PX);
  }

  /**
   * Bindings for the world widget css.
   */
  public interface Css extends CssResource {

    public String root();

    public String display();

    public String lineSpan();

    public String cursor();

    public String overlay();

    public String word();

  }

  /**
   * World widget resource bundle.
   */
  public interface Resources extends ClientBundle {

    @Source("CodeEditorWidget.css")
    public Css css();

  }

  public static final Resources RESOURCES = GWT.create(Resources.class);
  static { RESOURCES.css().ensureInjected(); }

}
