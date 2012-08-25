package org.au.tonomy.client.codemirror;

import com.google.gwt.core.client.JavaScriptObject;
/**
 * Contains all the relevant information about a change made in the
 * editor.
 */
public class ChangeEvent extends JavaScriptObject {

  protected ChangeEvent() { }

  /**
   * Returns the (pre-change) position where the change started.
   */
  public final native Position getFrom() /*-{
    return this.from;
  }-*/;

  /**
   * Returns the (pre-change) position where the change ended.
   */
  public final native Position getTo() /*-{
    return this.to;
  }-*/;

  /**
   * If multiple changes happened this will return the next change
   * event (which may itself have a next). Otherwise null is returned.
   */
  public final native ChangeEvent getNext() /*-{
    return this.next;
  }-*/;

  /**
   * Returns the number of lines replaced at the changed range.
   */
  public final native int getTextLineCount() /*-{
    return this.text.length;
  }-*/;

  /**
   * Returns the index'th line replaced at the changed range.
   */
  public final native String getTextLine(int index) /*-{
    return this.text[index];
  }-*/;

}
