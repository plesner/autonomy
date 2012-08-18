package org.au.tonomy.client.fileagent;

import java.util.HashMap;
import java.util.Map;

import org.au.tonomy.client.util.PromiseUtil;
import org.au.tonomy.shared.util.Assert;
import org.au.tonomy.shared.util.Factory;
import org.au.tonomy.shared.util.ICallback;
import org.au.tonomy.shared.util.Promise;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayMixed;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
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
    MessageEvent.addMessageHandler(new MessageHandler() {
      @Override
      public void onMessage(MessageEvent event) {
        dispatchIncoming(event);
      }
    });
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
        handleFrameConnect(event, (int) array.getNumber(2));
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
  private void handleFrameConnect(MessageEvent source, int attempt) {
    if (this.source != null)
      return;
    this.source = source;
    MessageBuilder pending = pendingMessages.remove(attempt);
    pending.result.fulfill(null);
  }

  /**
   * Forwards a response to the appropriate pending message.
   */
  private void handleRespond(String response, int id) {
    MessageBuilder pending = pendingMessages.remove(id);
    Response result = Response.create((JavaScriptObject) PromiseUtil.parseJson(response));
    if (result.hasFailed()) {
      pending.result.fail(new RuntimeException(result.getError()));
    } else {
      pending.result.fulfill(result.getValue());
    }
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
    Promise<Object> result = Promise.newEmpty();
    keepAttaching(result, 5000);
    return result;
  }

  /**
   * Keep trying to create a connection. As soon as one attempt fails
   * we try again.
   */
  private void keepAttaching(final Promise<Object> result, final int timeoutMs) {
    startAttempt(nextMessageId++, timeoutMs).onResolved(new ICallback<Object>() {
      @Override
      public void onSuccess(Object value) {
        result.fulfill(value);
      }
      @Override
      public void onFailure(Throwable error) {
        keepAttaching(result, timeoutMs);
      }
    });
  }

  /**
   * Makes an attempt to connect with the frame.
   */
  private Promise<Object> startAttempt(final int attempt, int timeoutMs) {
    // Try to attach a hidden frame from the agent.
    final Promise<Object> attemptPromise = Promise.newEmpty();
    pendingMessages.put(attempt, newMessage("").setResult(attemptPromise));
    final Document document = Document.get();
    final IFrameElement frame = document.createIFrameElement();
    frame.getStyle().setDisplay(Display.NONE);
    String query = "?attempt=" + attempt +
        "&target_origin=" + URL.encodeQueryString(getOrigin());
    frame.setSrc(root + query);
    document.getBody().appendChild(frame);
    // After the given timeout we check on the state of this attempt
    // and if it's failed we cancel it.
    Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {
      @Override
      public boolean execute() {
        if (attemptPromise.isResolved())
          return false;
        pendingMessages.remove(attempt);
        document.getBody().removeChild(frame);
        attemptPromise.fail(new RuntimeException("Attempt timed out."));
        return false;
      }
    }, timeoutMs);
    // Set up the connection hook.
    return whenConnected(attemptPromise);
  }

  /**
   * Returns the origin of this frame which the proxy frame will use
   * to ensure that only this frame gets messages.
   */
  protected static String getOrigin() {
    String protocol = Location.getProtocol();
    // Firefox doesn't allow file origins.
    String origin = "file:".equals(protocol)
      ? "*"
      : protocol + "//" + Location.getHost() + Location.getPath();
    return origin;
  }

  /**
   * A wrapper around a response from the frame.
   */
  private static class Response extends JavaScriptObject {

    protected Response() { }

    /**
     * Is this a failure response?
     */
    public final native boolean hasFailed() /*-{
      return !!this.error;
    }-*/;

    /**
     * Returns this response's error message.
     */
    public final native String getError() /*-{
      return this.error;
    }-*/;

    /**
     * Returns this response's value.
     */
    public final native Object getValue() /*-{
      return this.value;
    }-*/;

    /**
     * Wraps a response around a plain object.
     */
    public static native Response create(JavaScriptObject obj) /*-{
      return obj;
    }-*/;

  }

}
