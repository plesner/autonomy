package org.au.tonomy.client.codemirror;

import org.au.tonomy.client.presentation.IEditorWidget.IChangeEvent;

import com.google.gwt.core.client.JavaScriptObject;
/**
 * Contains all the relevant information about a change made in the
 * editor.
 */
public class ChangeEvent extends JavaScriptObject implements IChangeEvent {

  protected ChangeEvent() { }

  @Override
  public final native Position getFrom() /*-{
    return this.from;
  }-*/;

  @Override
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

  @Override
  public final native int getTextLineCount() /*-{
    return this.text.length;
  }-*/;

  @Override
  public final native String getTextLine(int index) /*-{
    return this.text[index];
  }-*/;

}
