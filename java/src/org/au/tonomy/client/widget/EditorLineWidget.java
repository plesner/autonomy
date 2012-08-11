package org.au.tonomy.client.widget;

import java.util.LinkedList;
import java.util.List;

import org.au.tonomy.client.Console;
import org.au.tonomy.shared.util.Factory;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Node;
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

  private final LinkedList<EditorToken> tokens = Factory.newLinkedList();
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

  public int getContentTop() {
    return contents.getOffsetTop();
  }

  public int getCharacterWidth() {
    int charCount = contents.getInnerText().length();
    return charCount == 0 ? 0 : contents.getOffsetWidth() / charCount;
  }

  public void insert(int tokenIndex, List<EditorToken> tokens) {
    Node prev;
    if (tokenIndex == 0) {
      prev = null;
    } else {
      prev = tokens.get(tokenIndex).getElement();
    }
    for (EditorToken token : tokens) {
      Node next = token.getElement();
      if (prev == null) {
        contents.insertFirst(next);
      } else {
        contents.insertAfter(next, prev);
      }
    }
    this.tokens.addAll(tokenIndex, tokens);
  }

}
