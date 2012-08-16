package org.au.tonomy.client.fileagent;

import org.au.tonomy.client.util.Callback;
import org.au.tonomy.shared.util.Assert;
import org.au.tonomy.shared.util.Promise;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.Window.Location;

/**
 * A frame proxy for communicating with a local file proxy.
 */
public class FileAgent extends FrameProxy {

  private FileHandle root;

  public FileAgent(String root) {
    super(root);
  }

  public FileHandle getRoot() {
    return Assert.notNull(root);
  }

  @Override
  protected Promise<Object> whenConnected(Promise<Object> onAttached) {
    final Promise<Object> result = Promise.newEmpty();
    super.whenConnected(onAttached).onResolved(new Callback<Object>() {
      @Override
      public void onSuccess(Object value) {
        startSession().forwardTo(result);
      }
    });
    return result;
  }

  /**
   * Starts a session with the file agent.
   */
  private Promise<?> startSession() {
    Promise<JavaScriptObject> result = newMessage("start_session")
        .setOption("href", Location.getHref())
        .send();
    result.onResolved(new Callback<JavaScriptObject>() {
      @Override
      public void onSuccess(JavaScriptObject data) {
        root = new FileHandle(data, FileAgent.this);
      }
    });
    return result;
  }

}
