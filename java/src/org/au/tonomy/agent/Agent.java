package org.au.tonomy.agent;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

import org.au.tonomy.agent.Json.JsonMap;
import org.au.tonomy.shared.util.Assert;
import org.au.tonomy.shared.util.Exceptions;
import org.au.tonomy.shared.util.Factory;

/**
 * Dispatches api requests.
 */
public class Agent {

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
    return new JsonMap() {{
      put("session", genSessionId());
    }};
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
