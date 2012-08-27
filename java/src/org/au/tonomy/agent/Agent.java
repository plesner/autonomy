package org.au.tonomy.agent;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import org.au.tonomy.shared.util.Assert;
import org.au.tonomy.shared.util.Exceptions;
import org.au.tonomy.shared.util.Factory;

/**
 * Dispatches api requests.
 */
public class Agent {

  private final FileSystem fileSystem = new FileSystem(Arrays.asList(new File("/Users/plesner/Documents/autonomy/java/test/org/au/tonomy/shared/syntax/testdata")));
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
  public Object handleStartSession(RequestInfo request) {
    String href = request.getParameter("href", "(unknown client)");
    System.out.println("Starting session with " + href + ".");
    final String id = genSessionId();
    getOrCreateSession(id);
    return ServerJson
        .getFactory()
        .newMap()
        .set("session", id);
  }

  /**
   * Returns the session with the given id or creates it if it doesn't
   * exist.
   */
  private Session getOrCreateSession(String id) {
    Session current = sessions.get(id);
    if (current == null) {
      current = new Session(fileSystem);
      sessions.put(id, current);
    }
    return current;
  }

  @Handler("fileroots")
  public Object handleFileRoots(RequestInfo request) {
    String sessionId = request.getParameter("session", "");
    Session session = sessions.get(sessionId);
    return session.getRoots();
  }

  @Handler("ls")
  public Object handleListFiles(RequestInfo request) {
    String fileId = request.getParameter("file", "");
    String sessionId = request.getParameter("session", "");
    Session session = sessions.get(sessionId);
    return session.listFiles(Integer.parseInt(fileId));
  }

  @Handler("read")
  public Object handleRead(RequestInfo request) {
    String fileId = request.getParameter("file", "");
    String sessionId = request.getParameter("session", "");
    Session session = sessions.get(sessionId);
    return session.readFile(Integer.parseInt(fileId));
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
  public Object dispatch(String handlerName, RequestInfo info) {
    Method method = getHandlers().get(handlerName);
    try {
      return Assert.notNull(method).invoke(this, info);
    } catch (IllegalArgumentException iae) {
      throw Exceptions.propagate(iae);
    } catch (IllegalAccessException iae) {
      throw Exceptions.propagate(iae);
    } catch (InvocationTargetException ite) {
      throw Exceptions.propagate(ite);
    }
  }

}
