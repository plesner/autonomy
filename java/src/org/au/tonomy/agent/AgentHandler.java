package org.au.tonomy.agent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Formatter;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.au.tonomy.shared.util.Assert;
import org.au.tonomy.shared.util.Exceptions;
import org.au.tonomy.shared.util.Factory;
import org.au.tonomy.shared.util.Pair;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

public class AgentHandler extends AbstractHandler {

  private static final Logger LOG = Log.getLogger(AgentHandler.class);
  private final Agent agent;

  public AgentHandler(Agent agent) {
    this.agent = agent;
  }

  @Override
  public void handle(String path, Request baseRequest, HttpServletRequest request,
      HttpServletResponse response) throws IOException, ServletException {
    Pair<EndPoint, Matcher> target = endPoints.find(path);
    EndPoint endPoint = target.getFirst();
    Matcher matcher = target.getSecond();
    String str;
    try {
      RequestInfo info = new RequestInfo(path, request, matcher);
      str = endPoint.getResponse(info);
    } catch (RuntimeException re) {
      LOG.warn(re.getMessage());
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      baseRequest.setHandled(true);
      return;
    }
    response.setStatus(endPoint.getStatusCode());
    if (str != null) {
      response.setContentType(endPoint.contentType);
      response.getWriter().print(str);
    }
    baseRequest.setHandled(true);
  }

  /**
   * Dispatches an api request to the agent.
   */
  private String getApiResponse(RequestInfo info) {
    final Object obj = agent.dispatch(info.getGroup(1), info);
    return ServerJson.stringify(ServerJson
        .getFactory()
        .newMap()
        .set("value", obj));
  }

  /**
   * The index endpoint which returns the bridge page.
   */
  private String getIndexPage(RequestInfo info) {
    String origin = info.getParameter("origin", "*");
    String methods = getMethodMap();
    String attempt = info.getParameter("attempt", "null");
    return new Formatter()
        .format(getBridgeTemplate(), origin, methods, attempt)
        .out()
        .toString();
  }

  private String methodHandlerMapCache;
  private String getMethodMap() {
    if (methodHandlerMapCache == null) {
      Map<String, Boolean> map = Factory.newTreeMap();
      for (String method : Agent.getHandlerNames())
        map.put(method, true);
      methodHandlerMapCache = ServerJson.stringify(map);
    }
    return methodHandlerMapCache;
  }

  private static final String BRIDGE_HTML =
      "<html><head><script>\n{connector}</script></head><body></body></html>";
  private String bridgeTemplateCache;

  /**
   * Returns the bridge page source code.
   */
  private String getBridgeTemplate() {
    if (bridgeTemplateCache == null)
      bridgeTemplateCache = BRIDGE_HTML.replace("{connector}", getConnectorSource());
    return bridgeTemplateCache;
  }

  /**
   * Reads the source of the connector script.
   */
  private String getConnectorSource() {
    String packageName = Main.class.getPackage().getName();
    String fileName = packageName.replace('.', '/') + "/connector.js";
    try {
      return readResource(fileName);
    } catch (IOException ioe) {
      throw Exceptions.propagate(ioe);
    }
  }

  /**
   * Reads a string resource into a string.
   */
  private String readResource(String name) throws IOException {
    ClassLoader loader = this.getClass().getClassLoader();
    InputStream in = loader.getResourceAsStream(name);
    Assert.notNull(in);
    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
    StringBuilder buf = new StringBuilder();
    while (reader.ready())
      buf.append(reader.readLine()).append('\n');
    return buf.toString();
  }

  /**
   * A handler for a particular end point.
   */
  private static class EndPoint {

    private final String contentType;

    public EndPoint(String contentType) {
      this.contentType = contentType;
    }

    public EndPoint() {
      this(null);
    }

    /**
     * The status code to return.
     */
    public int getStatusCode() {
      return HttpServletResponse.SC_OK;
    }

    /**
     * Returns the response string for this endpoint. If null is returned
     * no response will be sent.
     */
    public String getResponse(RequestInfo info) {
      return null;
    }

  }

  /**
   * Utility class for matching against a list of endpoints.
   */
  private static class EndPointList {

    private EndPoint fallback = null;
    private final List<Pair<Pattern, EndPoint>> entries = Factory.newArrayList();

    protected void add(String regexp, EndPoint endPoint) {
      entries.add(Pair.of(Pattern.compile(regexp), endPoint));
    }

    /**
     * Sets the fallback endpoint to use when no others match.
     */
    protected void setFallback(EndPoint value) {
      this.fallback = value;
    }

    /**
     * Returns the first endpoint that matches the given path.
     */
    public Pair<EndPoint, Matcher> find(String path) {
      for (Pair<Pattern, EndPoint> entry : entries) {
        Matcher matcher = entry.getFirst().matcher(path);
        if (matcher.matches()) {
          return Pair.of(entry.getSecond(), matcher);
        }
      }
      return Pair.of(fallback, null);
    }

  }

  /**
   * The endpoint that reports an error if an unknown path is requested.
   */
  private final EndPoint notFoundEndpoint = new EndPoint() {

    @Override
    public int getStatusCode() {
      return HttpServletResponse.SC_NOT_FOUND;
    }

    @Override
    public String getResponse(RequestInfo info) {
      LOG.warn("Unknown endpoint", info.getPath());
      return null;
    }

  };

  private final EndPointList endPoints = new EndPointList() {{
    setFallback(notFoundEndpoint);
    add("/", new EndPoint("text/html") {
      @Override
      public String getResponse(RequestInfo info) {
        return getIndexPage(info);
      }
    });
    add("/json/(\\w+)", new EndPoint() {
      public String getResponse(RequestInfo info) {
        return getApiResponse(info);
      }
    });
    add("/favicon.ico", new EndPoint() {
      @Override
      public int getStatusCode() {
        return HttpServletResponse.SC_NOT_FOUND;
      }
    });
  }};

}