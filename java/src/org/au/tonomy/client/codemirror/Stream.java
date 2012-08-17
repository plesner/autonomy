package org.au.tonomy.client.codemirror;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * A stream of codemirror input.
 */
public class Stream extends JavaScriptObject {

  protected Stream() { }

  /**
   * Shortcut for eatWhile when matching white-space.
   */
  public final native boolean eatSpace() /*-{
    return this.eatSpace();
  }-*/;

  /**
   * Returns the next character in the stream and advances it. Also
   * returns undefined when no more characters are available.
   */
  public final native String next() /*-{
    return this.next();
  }-*/;

  /**
   * Returns the next character in the stream without advancing it.
   * Will return an empty string at the end of the line.
   */
  public final native String peek() /*-{
    return this.peek();
  }-*/;

  /**
   * Are we at the end of a line?
   */
  public final native boolean atEndOfLine() /*-{
    return this.eol();
  }-*/;

}
