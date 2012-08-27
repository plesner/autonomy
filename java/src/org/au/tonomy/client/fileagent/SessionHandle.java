package org.au.tonomy.client.fileagent;

import java.util.List;

import org.au.tonomy.client.fileagent.FrameProxy.Method;
import org.au.tonomy.shared.util.Factory;
import org.au.tonomy.shared.util.IFunction;
import org.au.tonomy.shared.util.Promise;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayMixed;
/**
 * The data that describes a session with the file agent.
 */
public class SessionHandle {

  private final SessionJson data;
  private final FileAgent agent;

  public SessionHandle(Object data, FileAgent agent) {
    this.data = SessionJson.wrap(data);
    this.agent = agent;
  }

  /**
   * Returns the unique identifier for this session.
   */
  public String getId() {
    return data.getId();
  }

  public Promise<List<FileHandle>> getRoots() {
    return agent.newMessage(Method.GET, "fileroots")
        .setOption("session", data.getId())
        .send()
        .then(new IFunction<Object, List<FileHandle>>() {
          @Override
          public List<FileHandle> call(Object arg) {
            JsArrayMixed array = ((JavaScriptObject) arg).<JsArrayMixed>cast();
            List<FileHandle> result = Factory.newArrayList();
            for (int i = 0; i < array.length(); i++)
              result.add(new FileHandle(array.getObject(i), agent, SessionHandle.this));
            return result;
          }
        });
  }

  /**
   * Wrapper for session data objects.
   */
  private static class SessionJson extends JavaScriptObject {

    protected SessionJson() { }

    /**
     * Returns the session id.
     */
    public final native String getId() /*-{
      return this.session;
    }-*/;

    public static native SessionJson wrap(Object obj) /*-{
      return obj;
    }-*/;

  }

}
