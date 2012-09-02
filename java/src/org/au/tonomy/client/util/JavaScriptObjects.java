package org.au.tonomy.client.util;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Various helpers for working with js objects.
 */
public class JavaScriptObjects {

  /**
   * Sets a java script object property on a javascript object.
   */
  public static native <T extends JavaScriptObject> T setProperty(
      T object, String name, Object value) /*-{
    object[name] = value;
    return object;
  }-*/;

  public static native <T> T getProperty(JavaScriptObject object, String name) /*-{
    return object[name];
  }-*/;

}
