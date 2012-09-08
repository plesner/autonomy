package org.au.tonomy.shared.plankton;

import java.util.Map;
/**
 * A message sent over a plankton channel.
 */
public class Message implements IPlanktonDatable {

  private final String method;
  private final Object params;

  public Message(String method, Object params) {
    this.method = method;
    this.params = params;
  }

  @Override
  public Object toPlanktonData(IPlanktonFactory factory) {
    return factory.newMap().set("method", method).set("in", params);
  }

  public static Message parse(Object message) {
    Map<?, ?> map = (Map<?, ?>) message;
    return new Message((String) map.get("method"), map.get("in"));
  }

  public String getMethod() {
    return this.method;
  }

  public Object getParameters() {
    return this.params;
  }

}
