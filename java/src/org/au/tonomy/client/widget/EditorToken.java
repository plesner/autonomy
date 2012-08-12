package org.au.tonomy.client.widget;

import org.au.tonomy.shared.syntax.Token;
import org.au.tonomy.shared.util.Assert;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.SpanElement;
/**
 * A source token with some additional information that allows it to
 * be displayed in the editor.
 */
public class EditorToken extends Token {

  private SpanElement span;

  public EditorToken(Type type, String value) {
    super(type, value);
  }

  /**
   * Returns the singleton token widget factory.
   */
  public static IFactory<EditorToken> getWidgetFactory() {
    return FACTORY;
  }

  private static final IFactory<EditorToken> FACTORY = new AbstractFactory<EditorToken>() {
    @Override
    protected EditorToken newToken(Type type, String value) {
      return new EditorToken(type, value);
    }
  };

  /**
   * Returns this token's element, which must already have been
   * created.
   */
  public Node getElement() {
    return Assert.notNull(span);
  }

  /**
   * Returns this token's element, creating it if it doesn't already
   * exist.
   */
  public Node getOrCreateElement() {
    if (span == null) {
      span = Document.get().createSpanElement();
      span.setInnerText(getValue());
      span.setClassName(getCategory());
    }
    return span;
  }

}
