package org.au.tonomy.client.fileagent;

import org.au.tonomy.shared.ot.IDocument;

import com.google.gwt.core.client.JavaScriptObject;
/**
 * A document backed by json.
 */
public class JsonDocument extends JavaScriptObject implements IDocument {

  protected JsonDocument() { }

  @Override
  public final native JsonFingerprint getFingerprint() /*-{
    return this.fingerprint;
  }-*/;

  @Override
  public final native String getText() /*-{
    return this.text;
  }-*/;

}
