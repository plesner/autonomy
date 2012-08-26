package org.au.tonomy.client.fileagent;

import java.util.Map;

import org.au.tonomy.client.fileagent.FrameProxy.Method;
import org.au.tonomy.shared.source.ISourceEntry;
import org.au.tonomy.shared.util.Factory;
import org.au.tonomy.shared.util.IFunction;
import org.au.tonomy.shared.util.Promise;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayMixed;
/**
 * A handle to a file connected through the file agent.
 */
public class FileHandle implements ISourceEntry {

  private final FileHandleData data;
  private final FileAgent agent;
  private final SessionHandle session;

  public FileHandle(Object data, FileAgent agent, SessionHandle session) {
    this.data = FileHandleData.wrap(data);
    this.agent = agent;
    this.session = session;
  }

  @Override
  public boolean isFolder() {
    return !"file".equals(data.getType());
  }

  @Override
  public String getFullPath() {
    return data.getFullPath();
  }

  @Override
  public String getShortName() {
    return data.getShortName();
  }

  @Override
  public Promise<Map<String, FileHandle>> listEntries() {
    return agent.newMessage(Method.GET, "ls")
      .setOption("file", data.getId())
      .setOption("session", session.getId())
      .send()
      .then(new IFunction<Object, Map<String, FileHandle>>() {
        @Override
        public Map<String, FileHandle> call(Object value) {
          JsArrayMixed array = ((JavaScriptObject) value).<JsArrayMixed>cast();
          Map<String, FileHandle> list = Factory.newHashMap();
          for (int i = 0; i < array.length(); i++) {
            FileHandle handle = new FileHandle(array.getObject(i), agent, session);
            list.put(handle.getShortName(), handle);
          }
          return list;
        }
      });
  }

  /**
   * Returns the contents of this file.
   */
  @Override
  public Promise<String> readFile() {
    return agent.newMessage(Method.GET, "read")
        .setOption("file", data.getId())
        .setOption("session", session.getId())
        .send()
        .then(TO_STRING);
  }

  private static final IFunction<Object, String> TO_STRING = new IFunction<Object, String>() {
    @Override
    public String call(Object arg) {
      return (String) arg;
    }
  };

  /**
   * Wrapper for file handle data objects.
   */
  private static class FileHandleData extends JavaScriptObject {

    protected FileHandleData() { }

    public static native FileHandleData wrap(Object obj) /*-{
      return obj;
    }-*/;

    /**
     * Returns this file's handle id.
     */
    public final native int getId() /*-{
      return this.id;
    }-*/;

    /**
     * Returns a string identifying the type of this file.
     */
    public final native int getType() /*-{
      return this.type;
    }-*/;

    /**
     * Returns the full path of this file.
     */
    public final native String getFullPath() /*-{
      return this.path;
    }-*/;

    /**
     * Returns the short name of this file.
     */
    public final native String getShortName() /*-{
      return this.name;
    }-*/;

  }

}
