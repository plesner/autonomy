package org.au.tonomy.client.fileagent;

import com.google.gwt.core.client.JavaScriptObject;
/**
 * A wrapper around a message event.
 */
public class MessageEvent extends JavaScriptObject {

  protected MessageEvent() { }

  /**
   * Returns the parsed data payload of this message.
   */
  public final native Object getData() /*-{
    return JSON.parse(this.data);
  }-*/;

  /**
   * Installs the given message handler as a message listener on this
   * window.
   */
  public static native void addMessageHandler(MessageHandler handler) /*-{
    $wnd.addEventListener("message", function (event) {
      handler.@org.au.tonomy.client.fileagent.MessageHandler::onMessage(Lorg/au/tonomy/client/fileagent/MessageEvent;)(event);
    }, false);
  }-*/;

}
