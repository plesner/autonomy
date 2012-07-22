package org.au.tonomy.client.webmon;

import com.google.gwt.core.client.JavaScriptObject;
/**
 * A wrapper around a webmon counter.
 */
public class Counter extends JavaScriptObject {

  protected Counter() { }

  /**
   * Increments this counter by 1.
   */
  public final native void increment() /*-{
    this.increment();
  }-*/;

  /**
   * Creates a new webmon counter.
   */
  public static native Counter create(String name) /*-{
    return new $wnd.webmon.Counter(name);
  }-*/;

}
