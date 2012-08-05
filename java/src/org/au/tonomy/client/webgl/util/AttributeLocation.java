package org.au.tonomy.client.webgl.util;
/**
 * A wrapper around a shader attribute location.
 */
public class AttributeLocation {

  private final int index;

  public AttributeLocation(int index) {
    this.index = index;
  }
  
  public int getIndex() {
    return this.index;
  }

}
