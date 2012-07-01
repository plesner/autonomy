package org.au.tonomy.client.widget;

import org.au.tonomy.client.Console;
import org.au.tonomy.shared.syntax.Token;
import org.au.tonomy.shared.syntax.Tokenizer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class EditorLineWidget extends Composite {

  private static IEditorLineWidgetUiBinder BINDER = GWT .create(IEditorLineWidgetUiBinder.class);
  interface IEditorLineWidgetUiBinder extends UiBinder<Widget, EditorLineWidget> { }

  @UiField SpanElement contents;

  public EditorLineWidget() {
    initWidget(BINDER.createAndBindUi(this));
    configureEvents();
  }

  private void configureEvents() {
    this.addHandler(new MouseDownHandler() {
      @Override
      public void onMouseDown(MouseDownEvent event) {
        Console.log(event.getNativeEvent());
      }
    }, MouseDownEvent.getType());
  }

  public void update(String line) {
    contents.setInnerText("");
    for (Token token : Tokenizer.tokenize(line)) {
      SpanElement span = Document.get().createSpanElement();
      span.setInnerText(token.getValue());
      span.setClassName(token.getCategory());
      contents.appendChild(span);
    }
  }

  public int getContentTop() {
    return contents.getOffsetTop();
  }

  public int getCharacterWidth() {
    int charCount = contents.getInnerText().length();
    return charCount == 0 ? 0 : contents.getOffsetWidth() / charCount;
  }

}
