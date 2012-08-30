package org.au.tonomy.server.agent;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.au.tonomy.shared.plankton.DecodingError;
import org.au.tonomy.shared.plankton.Plankton;
import org.au.tonomy.shared.plankton.StringBinaryInputStream;
import org.au.tonomy.shared.plankton.StringBinaryOutputStream;
import org.au.tonomy.shared.util.Exceptions;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocket.OnTextMessage;
import org.eclipse.jetty.websocket.WebSocketServlet;
/**
 * The agent web socket.
 */
public class AgentSocket implements OnTextMessage {

  private final Agent agent;
  private Connection connection;

  private AgentSocket(Agent agent) {
    this.agent = agent;
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
  public void onMessage(String messageStr) {
    StringBinaryInputStream in = new StringBinaryInputStream(messageStr);
    List<?> message;
    try {
      message = (List<?>) Plankton.decode(in);
    } catch (DecodingError de) {
      throw Exceptions.propagate(de);
    }
    String method = (String) message.get(0);
    int id = (Integer) message.get(1);
    Map<?, ?> args = (Map<?, ?>) message.get(2);
    Object response = agent.dispatch(method, args);
    if (id != -1)
      sendResponse(id, response);
  }

  private void sendResponse(int id, Object response) {
    List<Object> message = Arrays.asList("respond", id, response);
    StringBinaryOutputStream out = new StringBinaryOutputStream();
    Plankton.encode(message, out);
    try {
      connection.sendMessage(out.flush());
    } catch (UnsupportedEncodingException uee) {
      throw Exceptions.propagate(uee);
    } catch (IOException ioe) {
      throw Exceptions.propagate(ioe);
    }
  }

  @SuppressWarnings("serial")
  public static WebSocketServlet newServlet(final Agent agent) {
    return new WebSocketServlet() {
      @Override
      public WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {
        return new AgentSocket(agent);
      }
    };
  }

}
