package org.au.tonomy.client;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Wrapper around the chrome console.
 */
public class Console {

  public static native void log(Object obj) /*-{
    console.log(obj);
  }-*/;

  public static native void log(JavaScriptObject obj) /*-{
    console.log(obj);
  }-*/;

}
