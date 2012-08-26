package org.au.tonomy.client.fileagent;

import org.au.tonomy.shared.util.Assert;
import org.au.tonomy.shared.util.IFunction;
import org.au.tonomy.shared.util.Promise;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * A frame proxy for communicating with a local file proxy.
 */
public class FileAgent extends FrameProxy {

  private SessionHandle session;

  public FileAgent(String root) {
    super(root);
  }

  public SessionHandle getSession() {
    return Assert.notNull(session);
  }

  @Override
  protected Promise<Object> whenConnected(Promise<Object> onAttached) {
    return super
        .whenConnected(onAttached)
        .lazyThen(new IFunction<Object, Promise<?>>() {
          @Override
          public Promise<?> call(Object arg) {
            return startSession();
          }
        });
  }

  /**
   * Starts a session with the file agent.
   */
  private Promise<SessionHandle> startSession() {
    return newMessage(Method.POST, "startsession")
        .setOption("href", getOrigin())
        .send()
        .then(new IFunction<Object, SessionHandle>() {
          @Override
          public SessionHandle call(Object data) {
            session = new SessionHandle((JavaScriptObject) data, FileAgent.this);
            return session;
          }
        });
  }

}
