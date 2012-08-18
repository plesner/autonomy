package org.au.tonomy.client.fileagent;

import java.util.Map;

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

  private final JavaScriptObject data;
  private final FileAgent agent;

  public FileHandle(JavaScriptObject data, FileAgent agent) {
    this.data = data;
    this.agent = agent;
  }

  private final native int getId() /*-{
    var data = this.@org.au.tonomy.client.fileagent.FileHandle::data;
    return data.id;
  }-*/;

  private native int getType() /*-{
    var data = this.@org.au.tonomy.client.fileagent.FileHandle::data;
    return data.type;
  }-*/;

  public final native String getFullPath() /*-{
    var data = this.@org.au.tonomy.client.fileagent.FileHandle::data;
    return data.path;
  }-*/;

  public final native String getShortName() /*-{
    var data = this.@org.au.tonomy.client.fileagent.FileHandle::data;
    return data.name;
  }-*/;

  @Override
  public boolean isFolder() {
    return !"file".equals(getType());
  }

  @Override
  public Promise<Map<String, FileHandle>> listEntries() {
    return agent.newMessage("get_file_list")
      .setOption("handle", getId())
      .send()
      .then(new IFunction<Object, Map<String, FileHandle>>() {
        @Override
        public Map<String, FileHandle> call(Object value) {
          JsArrayMixed array = ((JavaScriptObject) value).<JsArrayMixed>cast();
          Map<String, FileHandle> list = Factory.newHashMap();
          for (int i = 0; i < array.length(); i++) {
            FileHandle handle = new FileHandle(array.getObject(i), agent);
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
    return agent.newMessage("read_file")
        .setOption("id", getId())
        .send()
        .then(TO_STRING);
  }

  private static final IFunction<Object, String> TO_STRING = new IFunction<Object, String>() {
    @Override
    public String call(Object arg) {
      return (String) arg;
    }
  };

}
