package org.au.tonomy.client.agent;

import org.au.tonomy.shared.agent.pton.PSession;
import org.au.tonomy.shared.util.Assert;
import org.au.tonomy.shared.util.IFunction;
import org.au.tonomy.shared.util.Promise;

/**
 * A frame proxy for communicating with a local file proxy.
 */
public class FileAgent extends CrossDomainSocket {

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
    return newMessage("startsession")
        .setArgument("href", getOrigin())
        .send()
        .then(new IFunction<Object, SessionHandle>() {
          @Override
          public SessionHandle call(Object data) {
            session = new SessionHandle(FileAgent.this, PSession.parse(data));
            return session;
          }
        });
  }

}
