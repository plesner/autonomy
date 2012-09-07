package org.au.tonomy.client.agent;

import com.google.gwt.core.client.JavaScriptObject;
/**
 * A wrapper around a message event.
 */
public class MessageEvent extends JavaScriptObject {

  protected MessageEvent() { }

  /**
   * Returns the raw data payload of this message.
   */
  public final native String getPayload() /*-{
    return this.data;
  }-*/;

  /**
   * Returns the attempt number for the first incoming event.
   */
  public final native int getAttemptIndex() /*-{
    return this.data;
  }-*/;

  /**
   * Installs the given message handler as a message listener on this
   * window.
   */
  public static native void addMessageHandler(MessageHandler handler) /*-{
    $wnd.addEventListener("message", function (event) {
      handler.@org.au.tonomy.client.agent.MessageHandler::onMessage(Lorg/au/tonomy/client/agent/MessageEvent;)(event);
    }, false);
  }-*/;

}
