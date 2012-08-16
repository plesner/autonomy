package org.au.tonomy.client.fileagent;

import org.au.tonomy.client.Console;
import org.au.tonomy.shared.util.Promise;

import com.google.gwt.core.client.JavaScriptObject;
/**
 * A handle to a file connected through the file agent.
 */
public class FileHandle {

  private final JavaScriptObject data;
  private final FileAgent agent;

  public FileHandle(JavaScriptObject data, FileAgent agent) {
    Console.log(data);
    this.data = data;
    this.agent = agent;
  }

  public final native int getId() /*-{
    var data = this.@org.au.tonomy.client.fileagent.FileHandle::data;
    return data.handle;
  }-*/;

  public Promise<JavaScriptObject> getFileList() {
    return agent.newMessage("get_file_list")
        .setOption("handle", getId())
        .send();
  }

}
