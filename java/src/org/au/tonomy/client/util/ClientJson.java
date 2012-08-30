package org.au.tonomy.client.util;


public class ClientJson {

  /**
   * Wrapper around JSON.stringify.
   */
  public static native String stringify(Object obj) /*-{
    return JSON.stringify(obj);
  }-*/;


  /**
   * Wrapper around JSON.parse.
   */
  public static native Object parse(String str) /*-{
    return JSON.parse(str);
  }-*/;

}
