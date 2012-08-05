package org.au.tonomy.client.webmon;

import com.google.gwt.core.client.JavaScriptObject;

public class Variable<Self extends Variable<Self>> extends JavaScriptObject {

  protected Variable() { }

  /**
   * Sets the description of this variable.
   */
  public final native Self setDescription(String value) /*-{
    return this.setDescription(value);
  }-*/;

  /**
   * Marks this counter as a rate over the given duration. If null is
   * passed the default duration of one second is used.
   */
  public final native Self calcRate(String duration) /*-{
    return this.calcRate(duration);
  }-*/;

}
