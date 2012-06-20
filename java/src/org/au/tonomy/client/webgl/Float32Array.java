package org.au.tonomy.client.webgl;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayNumber;

/**
 * A wrapper around a JavaScript WebGL Float32Array object.
 */
public class Float32Array extends TypedArray {

  protected Float32Array() { }

  /**
   * Returns the element at the given numeric index.
   */
  public final native double get(int index) /*-{
    return this[index];
  }-*/;

  /**
   * Sets the element at the given numeric index to the given value.
   */
  public final native void set(int index, double value) /*-{
    this[index] = value;
  }-*/;

  /**
   * Create a new ArrayBuffer with enough bytes to hold array.length
   * elements of this typed array, then creates a typed array view
   * referring to the full buffer. The contents of the new view are
   * initialized to the contents of the given array or typed array,
   * with each element converted to the appropriate typed array type.
   */
  public static native Float32Array create(JsArrayNumber elements) /*-{
    return new Float32Array(elements);
  }-*/;

  /**
   * Creates a new array with the given entries.
   */
  public static Float32Array create(double... values) {
    JsArrayNumber array = JavaScriptObject.createArray().<JsArrayNumber>cast();
    for (double value : values)
      array.push(value);
    return create(array);
  }

}
