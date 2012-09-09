package org.au.tonomy.shared.plankton;

import java.util.Map;

import org.au.tonomy.shared.util.Assert;
import org.au.tonomy.shared.util.Factory;
import org.au.tonomy.shared.util.ICallback;
import org.au.tonomy.shared.util.IFunction;
import org.au.tonomy.shared.util.IThunk;
import org.au.tonomy.shared.util.Promise;

/**
 * A utility that handles dispatching incoming and outgoing messages
 * and assigning serial numbers.
 */
public class PackageProcessor {

  /**
   * A listener for incoming messages.
   */
  public interface IHandler {

    /**
     * An incoming message has been received.
     */
    public Promise<?> onMessage(RemoteMessage message);

  }

  private int nextSerial = 0;
  private final Map<Integer, Promise<Object>> pendingMessages = Factory.newHashMap();
  private final IThunk<String> postMessage;
  private IHandler handler = null;

  /**
   * Creates a new package processor that posts messages by calling the
   * given thunk.
   */
  public PackageProcessor(IThunk<String> postMessage) {
    this.postMessage = postMessage;
  }

  /**
   * Adds a message listener.
   */
  public void setHandler(IHandler handler) {
    Assert.isNull(this.handler);
    this.handler = Assert.notNull(handler);
  }

  /**
   * Sends the given message, returning a promise for the response.
   */
  private Promise<Object> sendMessage(RemoteMessage message) {
    int id = nextSerial++;
    Promise<Object> result = Promise.newEmpty();
    pendingMessages.put(id, result);
    RemotePackage fullMessage = RemotePackage
        .newBuilder()
        .setSerial(id)
        .setMessage(message)
        .build();
    sendPackage(fullMessage);
    return result;
  }

  /**
   * Puts a fully constructed package on the wire.
   */
  private void sendPackage(RemotePackage pack) {
    StringBinaryOutputStream out = new StringBinaryOutputStream();
    Plankton.encode(pack, out);
    postMessage.call(out.flush());
  }

  /**
   * Dispatches an incoming package.
   */
  public void dispatchPackage(String rawMessage) {
    Object message = Plankton.decode(new StringBinaryInputStream(rawMessage));
    RemotePackage pack = RemotePackage.parse(message);
    int serial = pack.getSerial();
    if (pack.hasMessage()) {
      processIncoming(serial, pack.getMessage());
    } else {
      Promise<Object> pending = pendingMessages.get(serial);
      if (pack.hasSuccessResponse()) {
        pending.fulfill(pack.getSuccessResponse());
      } else {
        pending.fail(new RemoteError(pack.getFailureResponse()));
      }
    }
  }

  /**
   * Processes an incoming request and arranges for the response to
   * be sent back.
   */
  private void processIncoming(final int serial, RemoteMessage message) {
    Promise<?> result = handler.onMessage(message);
    result.onResolved(new ICallback<Object>() {
      @Override
      public void onSuccess(Object value) {
        sendPackage(RemotePackage
            .newBuilder()
            .setSerial(serial)
            .setSuccessResponse(value)
            .build());
      }
      @Override
      public void onFailure(Throwable error) {
        sendPackage(RemotePackage
            .newBuilder()
            .setSerial(serial)
            .setFailureResponse(error.getMessage())
            .build());
      }
    });
  }

  /**
   * Returns a sender function that can be used with a plankton service
   * stub.
   */
  public IFunction<RemoteMessage, Promise<?>> newSender() {
    return new IFunction<RemoteMessage, Promise<?>>() {
      @Override
      public Promise<?> call(RemoteMessage message) {
        return sendMessage(message);
      }
    };
  }

}
