package org.au.tonomy.client.agent;

import org.au.tonomy.shared.agent.AgentService;
import org.au.tonomy.shared.agent.AgentService.StartSessionParameters;
import org.au.tonomy.shared.agent.SessionData;
import org.au.tonomy.shared.util.Assert;
import org.au.tonomy.shared.util.IFunction;
import org.au.tonomy.shared.util.Promise;

/**
 * A frame proxy for communicating with a local file proxy.
 */
public class FileAgent extends CrossDomainSocket {

  private SessionHandle session;
  private final AgentService.IClient client;

  public FileAgent(String root) {
    super(root);
    this.client = AgentService.newEncoder(newSender());
  }

  /**
   * Returns the client socket used to communicate with the server.
   */
  public AgentService.IClient getClient() {
    return this.client;
  }

  /**
   * Returns the handle for the current session.
   */
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
    return client
        .startSession(
            StartSessionParameters
                .newBuilder()
                .setHref(getOrigin())
                .build())
        .then(new IFunction<SessionData, SessionHandle>() {
          @Override
          public SessionHandle call(SessionData data) {
            session = new SessionHandle(FileAgent.this, data);
            return session;
          }
        });
  }


}
