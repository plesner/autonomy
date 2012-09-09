package org.au.tonomy.server.agent;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.au.tonomy.shared.agent.AgentService;
import org.au.tonomy.shared.plankton.PackageProcessor;
import org.au.tonomy.shared.plankton.RemoteMessage;
import org.au.tonomy.shared.util.Exceptions;
import org.au.tonomy.shared.util.IThunk;
import org.au.tonomy.shared.util.Promise;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocket.OnTextMessage;
import org.eclipse.jetty.websocket.WebSocketServlet;
/**
 * The agent web socket.
 */
public class AgentSocket implements OnTextMessage {

  private final PackageProcessor processor;
  private final AgentServiceImpl agent;
  private Connection connection;

  private AgentSocket(AgentServiceImpl agent) {
    this.agent = agent;
    this.processor = new PackageProcessor(new IThunk<String>() {
      @Override
      public void call(String value) {
        postOutgoing(value);
      }
    });
    this.processor.setHandler(new PackageProcessor.IHandler() {
      @Override
      public Promise<?> onMessage(RemoteMessage message) {
        return dispatchIncoming(message);
      }
    });
  }

  @Override
  public void onClose(int closeCode, String message) {
    this.connection = null;
  }

  @Override
  public void onOpen(Connection connection) {
    this.connection = connection;
  }

  @Override
  public void onMessage(String rawMessage) {
    processor.dispatchPackage(rawMessage);
  }

  private void postOutgoing(String message) {
    try {
      this.connection.sendMessage(message);
    } catch (IOException ioe) {
      throw Exceptions.propagate(ioe);
    }
  }

  private Promise<?> dispatchIncoming(RemoteMessage message) {
    return AgentService.dispatch(message, agent);
  }

  @SuppressWarnings("serial")
  public static WebSocketServlet newServlet(final AgentServiceImpl agent) {
    return new WebSocketServlet() {
      @Override
      public WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {
        return new AgentSocket(agent);
      }
    };
  }

}
