package org.au.tonomy.client.fileagent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.au.tonomy.shared.plankton.DecodingError;
import org.au.tonomy.shared.plankton.Plankton;
import org.au.tonomy.shared.plankton.StringBinaryInputStream;
import org.au.tonomy.shared.plankton.StringBinaryOutputStream;
import org.au.tonomy.shared.util.Assert;
import org.au.tonomy.shared.util.Exceptions;
import org.au.tonomy.shared.util.Factory;
import org.au.tonomy.shared.util.ICallback;
import org.au.tonomy.shared.util.Promise;

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
public abstract class CrossDomainSocket {

  private static final int RESPOND = 0;

  @SuppressWarnings("serial")
  private static final Map<String, Integer> DISPATCH = new HashMap<String, Integer>() {{
    put("respond", RESPOND);
  }};

  private final String root;
  private MessageEvent source;
  private int nextMessageId = 1;
  private Map<Integer, MessageBuilder> pendingMessages = Factory.newHashMap();

  public CrossDomainSocket(String root) {
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
    if (source == null) {
      // The initial request comes from the frame itself and we use it
      // to initialize the connection. It's not plankton encoded because
      // the frame doesn't know how to do that.
      handleFrameConnect(event, event.getAttemptIndex());
      return;
    }
    String rawMessage = event.getPayload();
    Object message;
    try {
      message = Plankton.decode(new StringBinaryInputStream(rawMessage));
    } catch (DecodingError de) {
      throw Exceptions.propagate(de);
    }
    List<?> parts = (List<?>) message;
    Integer index = DISPATCH.get((String) parts.get(0));
    if (index == null) {
      // forward to the subclass
    } else {
      switch (index) {
      case RESPOND:
        Object obj = parts.get(2);
        handleRespond(obj, (Integer) parts.get(1));
        break;
      }
    }
  }

  /**
   * Sets the source window of the iframe this file proxy is connected
   * through.
   */
  private void handleFrameConnect(MessageEvent source, int attempt) {
    Assert.isNull(this.source);
    this.source = source;
    MessageBuilder pending = pendingMessages.remove(attempt);
    pending.result.fulfill(null);
  }

  /**
   * Forwards a response to the appropriate pending message.
   */
  private void handleRespond(Object response, int id) {
    MessageBuilder pending = pendingMessages.remove(id);
    pending.result.fulfill(response);
  }

  /**
   * Creates a new message builder.
   */
  protected MessageBuilder newMessage(String endPoint) {
    return new MessageBuilder(endPoint);
  }

  /**
   * A utility for building and sending messages.
   */
  protected class MessageBuilder {

    private final String endPoint;
    private final Map<Object, Object> args = Factory.newHashMap();
    private int id = -1;
    private Promise<Object> result;

    private MessageBuilder(String endPoint) {
      this.endPoint = endPoint;
    }

    private MessageBuilder setResult(Promise<Object> result) {
      Assert.isNull(this.result);
      this.result = result;
      return this;
    }

    /**
     * Sets a string option on this message.
     */
    public MessageBuilder setArgument(Object key, Object value) {
      args.put(key, value);
      return this;
    }

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
      List<Object> message = Factory.newArrayList();
      message.add(endPoint);
      message.add(id);
      message.add(args);
      StringBinaryOutputStream out = new StringBinaryOutputStream();
      Plankton.encode(message, out);
      postMessage(out.flush());
    }

  }

  /**
   * Sends an object as a message to the frame connected to this proxy.
   */
  private native void postMessage(String message) /*-{
    var source = this.@org.au.tonomy.client.fileagent.CrossDomainSocket::source;
    var targetOrigin = this.@org.au.tonomy.client.fileagent.CrossDomainSocket::root;
    source.source.postMessage(message, targetOrigin);
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
        "&origin=" + URL.encodeQueryString(getOrigin());
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

}
