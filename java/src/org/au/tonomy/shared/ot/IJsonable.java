package org.au.tonomy.shared.ot;
/**
 * A jsonable object will be asked to convert itself to json before
 * it is stringified.
 */
public interface IJsonable {

  /**
   * Returns an json value that represents this object.
   */
  public Object toJson();

}
