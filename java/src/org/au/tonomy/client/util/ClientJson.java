package org.au.tonomy.client.util;

import org.au.tonomy.shared.util.IJsonFactory;
import org.au.tonomy.shared.util.IJsonFactory.IJsonArray;
import org.au.tonomy.shared.util.IJsonFactory.IJsonMap;
import org.au.tonomy.shared.util.IJsonable;

import com.google.gwt.core.client.JavaScriptObject;

public class ClientJson {

  private static class JsonMap extends JavaScriptObject implements IJsonMap {

    protected JsonMap() { }

    @Override
    public final native IJsonMap set(String key, Object value) /*-{
      this[key] = value;
      return this;
    }-*/;

  }

  private static class JsonArray extends JavaScriptObject implements IJsonArray {

    protected JsonArray() { }

    @Override
    public final native IJsonArray push(Object value) /*-{
      this.push(value);
      return this;
    }-*/;

  }

  private static final IJsonFactory FACTORY = new IJsonFactory() {

    @Override
    public final native JsonMap newMap() /*-{
      return {};
    }-*/;

    @Override
    public final native JsonArray newArray() /*-{
      return [];
    }-*/;

  };

  /**
   * Converts the given object to a json object and then stringifies
   * the result.
   */
  public static String stringify(IJsonable obj) {
    return stringify(obj.toJson(FACTORY));
  }

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
