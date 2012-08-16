package org.au.tonomy.client.fileagent;

import com.google.gwt.event.shared.EventHandler;
/**
 * Handler for inter-frame messages.
 */
public interface MessageHandler extends EventHandler {

  /**
   * Called when a message event is fired.
   */
  public void onMessage(MessageEvent event);

}
