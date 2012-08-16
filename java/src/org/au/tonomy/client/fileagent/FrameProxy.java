package org.au.tonomy.client.fileagent;

import java.util.HashMap;
import java.util.Map;

import org.au.tonomy.client.util.PromiseUtil;
import org.au.tonomy.shared.util.Assert;
import org.au.tonomy.shared.util.Factory;
import org.au.tonomy.shared.util.Promise;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayMixed;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.IFrameElement;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Window.Location;
/**
 * An abstract class for communicating with an external process through
 * a proxy frame.
 */
public abstract class FrameProxy {

  private static final int FRAME_CONNECT = 0;
  private static final int RESPOND = 1;

  @SuppressWarnings("serial")
  private static final Map<String, Integer> DISPATCH = new HashMap<String, Integer>() {{
    put("frameConnect", FRAME_CONNECT);
    put("respond", RESPOND);
  }};

  private final String root;
  private MessageEvent source;
  private int nextMessageId = 1;
  private Map<Integer, MessageBuilder> pendingMessages = Factory.newHashMap();

  public FrameProxy(String root) {
    this.root = root;
  }

  /**
   * Hook which subclasses can use to add initialization code to
   * set up the connection with the frame. The input promise will be
   * resolved when the frame is attached, the returned promise is the
   * one that will be returned from {@link #attach()}.
   */
  protected Promise<Object> whenConnected(Promise<Object> onAttached) {
    return onAttached;
  }

  /**
   * Dispatches an incoming event object to a method call on this object.
   */
  private void dispatchIncoming(MessageEvent event) {
    Object message = event.getData();
    JsArrayMixed array = ((JavaScriptObject) message).<JsArrayMixed>cast();
    Integer index = DISPATCH.get(array.getString(0));
    if (index == null) {
      // forward to the subclass
    } else {
      switch (index) {
      case FRAME_CONNECT:
        handleFrameConnect(event);
        break;
      case RESPOND:
        handleRespond(array.getString(1), (int) array.getNumber(2));
        break;
      }
    }
  }

  /**
   * Sets the source window of the iframe this file proxy is connected
   * through.
   */
  private void handleFrameConnect(MessageEvent source) {
    Assert.isNull(this.source);
    this.source = source;
    MessageBuilder pending = pendingMessages.remove(0);
    pending.result.fulfill(null);
  }

  /**
   * Forwards a response to the appropriate pending message.
   */
  private void handleRespond(String response, int id) {
    MessageBuilder pending = pendingMessages.remove(id);
    pending.result.fulfill(PromiseUtil.parseJson(response));
  }

  /**
   * Creates a new message builder.
   */
  protected MessageBuilder newMessage(String method) {
    return new MessageBuilder(method);
  }

  /**
   * A utility for building and sending messages.
   */
  protected class MessageBuilder {

    private final JsArrayMixed message = JavaScriptObject.createArray().<JsArrayMixed>cast();
    private final JsArrayMixed options = JavaScriptObject.createArray().<JsArrayMixed>cast();
    private int id = -1;
    private Promise<Object> result;

    private MessageBuilder(String method) {
      this.message.push(method);
      this.message.push(this.options);
    }

    private MessageBuilder setResult(Promise<Object> result) {
      Assert.isNull(this.result);
      this.result = result;
      return this;
    }

    /**
     * Sets a string option on this message.
     */
    public final native MessageBuilder setOption(String name, String value) /*-{
      var options = this.@org.au.tonomy.client.fileagent.FrameProxy.MessageBuilder::options;
      options.push([name, value]);
      return this;
    }-*/;

    /**
     * Sets an int option on this message.
     */
    public final native MessageBuilder setOption(String name, int value) /*-{
      var options = this.@org.au.tonomy.client.fileagent.FrameProxy.MessageBuilder::options;
      options.push([name, value]);
      return this;
    }-*/;

    /**
     * Sends this message, returning a promise for the result.
     */
    public Promise<Object> send() {
      Assert.isNull(result);
      this.id = nextMessageId++;
      Promise<Object> result = Promise.newEmpty();
      this.result = result;
      pendingMessages.put(id, this);
      sendAsync();
      return result;
    }

    public void sendAsync() {
      this.message.push(this.id);
      postMessage(message);
    }

  }

  /**
   * Sends an object as a message to the frame connected to this proxy.
   */
  private native void postMessage(JavaScriptObject message) /*-{
    var source = this.@org.au.tonomy.client.fileagent.FrameProxy::source;
    var targetOrigin = this.@org.au.tonomy.client.fileagent.FrameProxy::root;
    source.source.postMessage(JSON.stringify(message), targetOrigin);
  }-*/;

  /**
   * Attach this file proxy to the given document. Returns a promise
   * that resolves to true once a connection is established.
   */
  public Promise<Object> attach() {
    Promise<Object> attachPromise = Promise.newEmpty();
    pendingMessages.put(0, newMessage("").setResult(attachPromise));
    MessageEvent.addMessageHandler(new MessageHandler() {
      @Override
      public void onMessage(MessageEvent event) {
        dispatchIncoming(event);
      }
    });
    Document document = Document.get();
    IFrameElement frame = document.createIFrameElement();
    frame.getStyle().setDisplay(Display.NONE);
    frame.setSrc(root + "?target_origin=" + URL.encodeQueryString(Location.getHref()));
    document.getBody().appendChild(frame);
    return whenConnected(attachPromise);
  }

}
