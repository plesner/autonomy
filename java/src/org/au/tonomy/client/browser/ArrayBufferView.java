package org.au.tonomy.client.browser;

import com.google.gwt.core.client.JavaScriptObject;
/**
 * The ArrayBufferView type holds information shared among all of the
 * types of views of ArrayBuffers.
 *
 * See http://www.khronos.org/registry/typedarray/specs/latest/#6.
 */
public class ArrayBufferView extends JavaScriptObject {

  protected ArrayBufferView() { }

  /**
   * Returns the ArrayBuffer that this ArrayBufferView references.
   */
  public final native ArrayBuffer getBuffer() /*-{
    return this.buffer;
  }-*/;

  /**
   * The offset of this ArrayBufferView from the start of its ArrayBuffer,
   * in bytes, as fixed at construction time.
   */
  public final native int getByteOffset() /*-{
    return this.byteOffset;
  }-*/;

  /**
   * The length of the ArrayBufferView in bytes, as fixed at construction
   * time.
   */
  public final native int getByteLength() /*-{
    return this.byteLength;
  }-*/;

}
