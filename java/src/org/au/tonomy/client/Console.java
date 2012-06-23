package org.au.tonomy.client;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Wrapper around the chrome console.
 */
public class Console {

  /**
   * Logs this object to the console.
   */
  public static native void log(Object obj) /*-{
    if ($wnd.console && $wnd.console.log)
      $wnd.console.log(obj);
  }-*/;

  /**
   * Logs this JS object to the console.
   */
  public static native void log(JavaScriptObject obj) /*-{
    if ($wnd.console && $wnd.console.log)
      $wnd.console.log(obj);
  }-*/;

}
