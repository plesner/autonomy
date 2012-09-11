package org.au.tonomy.client.agent;

import org.au.tonomy.client.util.ClientPromise;
import org.au.tonomy.shared.plankton.PackageProcessor;
import org.au.tonomy.shared.plankton.RemoteMessage;
import org.au.tonomy.shared.util.Assert;
import org.au.tonomy.shared.util.ICallback;
import org.au.tonomy.shared.util.IFunction;
import org.au.tonomy.shared.util.IThunk;
import org.au.tonomy.shared.util.Promise;

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

  private final String root;
  private final PackageProcessor processor;
  private Promise<Object> connectPromise;
  private MessageEvent source;

  public CrossDomainSocket(String root) {
    this.root = root;
    this.processor = new PackageProcessor(new IThunk<String>() {
      @Override
      public void call(String value) {
        postMessage(value);
      }
    });
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
    if (source == null && connectPromise != null) {
      // The initial request comes from the frame itself and we use it
      // to initialize the connection.
      handleFrameConnect(event);
    } else {
      processor.dispatchPackage(event.getPayload());
    }
  }

  /**
   * Sets the source window of the iframe this file proxy is connected
   * through.
   */
  private void handleFrameConnect(MessageEvent source) {
    Assert.isNull(this.source);
    Assert.notNull(this.connectPromise);
    this.source = source;
    this.connectPromise.fulfill(null);
    this.connectPromise = null;
  }

  /**
   * Sends an object as a message to the frame connected to this proxy.
   */
  private native void postMessage(String message) /*-{
    var source = this.@org.au.tonomy.client.agent.CrossDomainSocket::source;
    var targetOrigin = this.@org.au.tonomy.client.agent.CrossDomainSocket::root;
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
    startAttempt(timeoutMs).onResolved(new ICallback<Object>() {
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
  private Promise<Object> startAttempt(int timeoutMs) {
    // Try to attach a hidden frame from the agent.
    final Promise<Object> attemptPromise = Promise.newEmpty();
    final Document document = Document.get();
    final IFrameElement frame = document.createIFrameElement();
    frame.getStyle().setDisplay(Display.NONE);
    String query = "?origin=" + URL.encodeQueryString(getOrigin());
    frame.setSrc(root + query);
    document.getBody().appendChild(frame);
    // After the given timeout we check on the state of this attempt
    // and if it's failed we cancel it.
    ClientPromise
        .failAfterDelay(timeoutMs, null)
        .forwardTo(attemptPromise)
        .onFail(new IThunk<Object>() {
          @Override
          public void call(Object value) {
            if (!attemptPromise.hasSucceeded()) {
              document.getBody().removeChild(frame);
            }
          }
        });
    // Set up the connection hook.
    this.connectPromise = attemptPromise;
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

  public IFunction<RemoteMessage, Promise<?>> newSender() {
    return processor.newSender();
  }

}
