package org.au.tonomy.agent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.au.tonomy.shared.util.Assert;
import org.au.tonomy.shared.util.Exceptions;


/**
 * The main class for the file agent.
 */
public class Agent {

  private static final String BRIDGE_TEMPLATE =
      "<html><head><script>\n{connector}</script></head><body></body></html>";
  private String bridgePageCache;

  private Agent() { }

  /**
   * Returns the bridge page source code.
   */
  private String getBridgePage() {
    if (bridgePageCache == null)
      bridgePageCache = BRIDGE_TEMPLATE.replace("{connector}", getConnectorSource());
    return bridgePageCache;
  }

  /**
   * Reads the source of the connector script.
   */
  private String getConnectorSource() {
    String packageName = Agent.class.getPackage().getName();
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

  public static void main(String[] args) {
    System.out.println(new Agent().getBridgePage());
  }

}
