package org.au.tonomy.server.agent;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.au.tonomy.shared.ot.IDocument;
import org.au.tonomy.shared.ot.Md5Fingerprint;
import org.au.tonomy.shared.ot.PojoDocument;
import org.au.tonomy.shared.ot.Transform;
import org.au.tonomy.shared.util.Assert;
import org.au.tonomy.shared.util.Exceptions;
import org.au.tonomy.shared.util.Factory;

/**
 * Dispatches api requests.
 */
public class Agent {

  private final FileSystem fileSystem = new FileSystem(
      PojoDocument.newProvider(Md5Fingerprint.getProvider()),
      Arrays.asList(new File("/Users/plesner/Documents/autonomy/java/test/org/au/tonomy/shared/syntax/testdata")));
  private final Map<String, Session> sessions = Factory.newHashMap();
  private int nextSessionId = 0;

  /**
   * Marker for methods that will be used to respond to api requests.
   */
  @Retention(RetentionPolicy.RUNTIME)
  private static @interface Handler {
    public String value();
  }

  @Handler("startsession")
  public Session handleStartSession(Map<?, ?> request) {
    String href = (String) request.get("href");
    System.out.println("Starting session with " + href + ".");
    final String id = genSessionId();
    return getOrCreateSession(id);
  }

  /**
   * Returns the session with the given id or creates it if it doesn't
   * exist.
   */
  private Session getOrCreateSession(String id) {
    Session current = sessions.get(id);
    if (current == null) {
      current = new Session(id, fileSystem);
      sessions.put(id, current);
    }
    return current;
  }

  @Handler("fileroots")
  public List<SessionFile> handleFileRoots(Map<?, ?> request) {
    String sessionId = (String) request.get("session");
    Session session = sessions.get(sessionId);
    return session.getRoots();
  }

  @Handler("ls")
  public List<SessionFile> handleListFiles(Map<?, ?> request) {
    int fileId = (Integer) request.get("file");
    String sessionId = (String) request.get("session");
    Session session = sessions.get(sessionId);
    return session.listFiles(fileId);
  }

  @Handler("read")
  public IDocument handleRead(Map<?, ?> request) {
    int fileId = (Integer) request.get("file");
    String sessionId = (String) request.get("session");
    Session session = sessions.get(sessionId);
    return session.readFile(fileId);
  }

  @Handler("changefile")
  public Object handleChangeFile(Map<?, ?> request) {
    int fileId = (Integer) request.get("file");
    String sessionId = (String) request.get("session");
    Transform transform = Transform.unpack((List<?>) request.get("transform"));
    Session session = sessions.get(sessionId);
    session.changeFile(fileId, transform);
    return null;
  }

  @Handler("savependingchanges")
  public Object handleSaveFileChanges(Map<?, ?> request) {
    int fileId = (Integer) request.get("file");
    String sessionId = (String) request.get("session");
    Session session = sessions.get(sessionId);
    session.savePendingChanges(fileId);
    return null;
  }

  private synchronized String genSessionId() {
    return Integer.toHexString(nextSessionId++);
  }

  /**
   * Returns the names of the handlers understood by this agent api.
   */
  public static Set<String> getHandlerNames() {
    return getHandlers().keySet();
  }

  private static Map<String, Method> handlersCache = null;
  /**
   * Returns a map from handler names to the associated methods.
   */
  private static Map<String, Method> getHandlers() {
    if (handlersCache == null) {
      Map<String, Method> handlers = Factory.newHashMap();
      for (Method method : Agent.class.getDeclaredMethods()) {
        Handler annot = method.getAnnotation(Handler.class);
        if (annot != null)
          handlers.put(annot.value(), method);
      }
      handlersCache = handlers;
    }
    return handlersCache;
  }

  /**
   * Invokes the method with the given handler name, passing the given
   * request info as an argument.
   */
  public Object dispatch(String handlerName, Map<?, ?> args) {
    Method method = getHandlers().get(handlerName);
    try {
      return Assert.notNull(method).invoke(this, args);
    } catch (IllegalArgumentException iae) {
      throw Exceptions.propagate(iae);
    } catch (IllegalAccessException iae) {
      throw Exceptions.propagate(iae);
    } catch (InvocationTargetException ite) {
      throw Exceptions.propagate(ite);
    }
  }

}