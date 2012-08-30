package org.au.tonomy.client.browser;

import com.google.gwt.core.client.JavaScriptObject;
/**
 * The ArrayBuffer type describes a buffer used to store data for the
 * array buffer views.
 *
 * See http://www.khronos.org/registry/typedarray/specs/latest/#ARRAYBUFFER.
 */
public class ArrayBuffer extends JavaScriptObject {

  protected ArrayBuffer() { }

  /**
   * The length of the ArrayBuffer in bytes, as fixed at construction
   * time.
   */
  public final native int getByteLength() /*-{
    return this.byteLength;
  }-*/;

}
