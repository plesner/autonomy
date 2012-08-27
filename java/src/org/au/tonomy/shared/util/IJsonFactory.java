package org.au.tonomy.shared.util;
/**
 * A factory for creating json objects. The server and client side use
 * different types objects to represent arrays and mapes and this
 * factory makes that possible.
 */
public interface IJsonFactory {

  /**
   * A json map builder.
   */
  public interface IJsonMap {

    public IJsonMap set(String key, Object value);

  }

  /**
   * A json array builder.
   */
  public interface IJsonArray {

    public IJsonArray push(Object value);

  }

  /**
   * Creates a new empty map object.
   */
  public IJsonMap newMap();

  /**
   * Creates a new empty array object.
   */
  public IJsonArray newArray();

}
