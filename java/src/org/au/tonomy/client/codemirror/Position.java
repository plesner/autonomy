package org.au.tonomy.client.codemirror;

import com.google.gwt.core.client.JavaScriptObject;
/**
 * A position within the editor.
 */
public class Position extends JavaScriptObject {

  protected Position() { }

  /**
   * Returns the line of this position.
   */
  public final native int getLine() /*-{
    return this.line;
  }-*/;

  /**
   * Returns the character position within the line.
   */
  public final native int getChar() /*-{
    return this.ch;
  }-*/;

}
