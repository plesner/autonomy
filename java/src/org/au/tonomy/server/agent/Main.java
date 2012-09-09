package org.au.tonomy.server.agent;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.au.tonomy.shared.util.Exceptions;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * The main class for the file agent.
 */
public class Main {

  private static final int PORT = 8040;

  private final Server server;

  private Main() {
    AgentServiceImpl agent = new AgentServiceImpl();
    ServletHandler handler = new ServletHandler();
    handler.addServletWithMapping(new ServletHolder(new TrampolineServlet()), "/");
    handler.addServletWithMapping(new ServletHolder(AgentSocket.newServlet(agent)), "/api");
    this.server = new Server(PORT);
    this.server.setHandler(handler);
  }

  public void start() throws Exception {
    this.server.start();
  }

  public void join() throws InterruptedException {
    this.server.join();
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
    Main main = new Main();
    main.start();
    main.join();
  }

}
