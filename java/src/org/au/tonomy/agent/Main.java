package org.au.tonomy.agent;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.au.tonomy.shared.util.Exceptions;
import org.eclipse.jetty.server.Server;

/**
 * The main class for the file agent.
 */
public class Main {

  private static final int PORT = 8040;

  private final Server server;

  private Main() {
    this.server = new Server(PORT);
    this.server.setHandler(new AgentHandler(new Agent()));
  }

  public void start() throws Exception {
    this.server.start();
  }

  private static void printMessage() {
    String host;
    try {
      host = InetAddress.getLocalHost().getHostAddress();
    } catch (UnknownHostException uhe) {
      throw Exceptions.propagate(uhe);
    }
    System.out.println("Running agent at http://" + host + ":" + PORT + ".");
  }

  private static void setupLogging() {
    System.setProperty("org.eclipse.jetty.LEVEL", "WARN");
  }

  public static void main(String[] args) throws Exception {
    setupLogging();
    printMessage();
    new Main().start();
  }

}
