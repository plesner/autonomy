package org.au.tonomy.client.codemirror;

import org.au.tonomy.client.presentation.IEditorWidget.IPosition;

import com.google.gwt.core.client.JavaScriptObject;
/**
 * A position within the editor.
 */
public class Position extends JavaScriptObject implements IPosition {

  protected Position() { }

  @Override
  public final native int getLine() /*-{
    return this.line;
  }-*/;

  @Override
  public final native int getChar() /*-{
    return this.ch;
  }-*/;

}
