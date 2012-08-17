package org.au.tonomy.client.fileagent;

import java.util.List;

import org.au.tonomy.shared.util.Factory;
import org.au.tonomy.shared.util.IFunction;
import org.au.tonomy.shared.util.Promise;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayMixed;
/**
 * A handle to a file connected through the file agent.
 */
public class FileHandle {

  private final JavaScriptObject data;
  private final FileAgent agent;

  public FileHandle(JavaScriptObject data, FileAgent agent) {
    this.data = data;
    this.agent = agent;
  }

  private final native int getId() /*-{
    var data = this.@org.au.tonomy.client.fileagent.FileHandle::data;
    return data.handle;
  }-*/;

  public final native String getName() /*-{
    var data = this.@org.au.tonomy.client.fileagent.FileHandle::data;
    return data.name;
  }-*/;

  public Promise<List<FileHandle>> getFileList() {
    return agent.newMessage("get_file_list")
      .setOption("handle", getId())
      .send()
      .then(new IFunction<Object, List<FileHandle>>() {
        @Override
        public List<FileHandle> call(Object value) {
          JsArrayMixed array = ((JavaScriptObject) value).<JsArrayMixed>cast();
          List<FileHandle> list = Factory.newArrayList();
          for (int i = 0; i < array.length(); i++)
            list.add(new FileHandle(array.getObject(i), agent));
          return list;
        }
      });
  }

  /**
   * Returns the contents of this file.
   */
  public Promise<String> read() {
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