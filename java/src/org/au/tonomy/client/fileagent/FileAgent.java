package org.au.tonomy.client.fileagent;

import org.au.tonomy.shared.util.Assert;
import org.au.tonomy.shared.util.IFunction;
import org.au.tonomy.shared.util.Promise;

import com.google.gwt.core.client.JavaScriptObject;

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
    return super
        .whenConnected(onAttached)
        .lazyThen(new IFunction<Object, Promise<?>>() {
          @Override
          public Promise<?> call(Object arg) {
            return startSession();
          }
        });
  }

  /**
   * Starts a session with the file agent.
   */
  private Promise<?> startSession() {
    return newMessage("start_session")
        .setOption("href", getOrigin())
        .send()
        .then(new IFunction<Object, Object>() {
          @Override
          public Object call(Object data) {
            root = new FileHandle((JavaScriptObject) data, FileAgent.this);
            return null;
          }
        });
  }

}
