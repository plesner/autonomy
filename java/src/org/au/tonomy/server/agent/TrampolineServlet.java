package org.au.tonomy.server.agent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Formatter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.au.tonomy.shared.util.Assert;
import org.au.tonomy.shared.util.Exceptions;
/**
 * A servlet that serves the trampoline frame.
 */
@SuppressWarnings("serial")
public class TrampolineServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    resp.setContentType("text/html");
    resp.setStatus(200);
    resp.getWriter().print(getIndexPage(req));
  }

  /**
   * The index endpoint which returns the bridge page.
   */
  private String getIndexPage(HttpServletRequest req) {
    String origin = req.getParameter("origin");
    String attempt = req.getParameter("attempt");
    return new Formatter()
        .format(getBridgeTemplate(), origin, attempt)
        .out()
        .toString();
  }

  private static final String BRIDGE_HTML =
      "<html><head><script>\n{trampoline}</script></head><body></body></html>";
  private String bridgeTemplateCache;

  /**
   * Returns the bridge page source code.
   */
  private String getBridgeTemplate() {
    if (bridgeTemplateCache == null)
      bridgeTemplateCache = BRIDGE_HTML.replace("{trampoline}", getTrampolineSource());
    return bridgeTemplateCache;
  }

  /**
   * Reads the source of the connector script.
   */
  private String getTrampolineSource() {
    String packageName = Main.class.getPackage().getName();
    String fileName = packageName.replace('.', '/') + "/trampoline.js";
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

}
