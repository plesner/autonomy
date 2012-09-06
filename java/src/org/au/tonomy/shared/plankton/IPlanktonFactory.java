package org.au.tonomy.shared.plankton;
/**
 * A factory for creating json objects. The server and client side use
 * different types objects to represent arrays and mapes and this
 * factory makes that possible.
 */
public interface IPlanktonFactory {

  /**
   * A json map builder.
   */
  public interface IPlanktonMap {

    public IPlanktonMap set(String key, Object value);

  }

  /**
   * A json array builder.
   */
  public interface IPlanktonArray {

    public IPlanktonArray push(Object value);

  }

  /**
   * Creates a new empty map object.
   */
  public IPlanktonMap newMap();

  /**
   * Creates a new empty array object.
   */
  public IPlanktonArray newArray();

}
