package org.au.tonomy.client.browser;

/**
 * Supertype for all the concrete typed array types.
 */
public abstract class TypedArray extends ArrayBufferView {

  protected TypedArray() { }

  public final native int getLength() /*-{
    return this.length;
  }-*/;

}
