package org.au.tonomy.gwt.client;
/**
 * Wrapper around the chrome console.
 */
public class Console {

  public static native void log(Object obj) /*-{
    console.log(obj);
  }-*/;

}
