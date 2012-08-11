package org.au.tonomy.client.widget;

import org.au.tonomy.shared.syntax.Token;
/**
 * A source token with some additional information that allows it to
 * be displayed in the editor.
 */
public class TokenWidget extends Token {

  public TokenWidget(Type type, String value) {
    super(type, value);
  }

}
