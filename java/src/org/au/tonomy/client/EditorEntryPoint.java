package org.au.tonomy.client;

import org.au.tonomy.client.fileagent.FileAgent;
import org.au.tonomy.client.util.Callback;
import org.au.tonomy.client.widget.EditorWidget;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;

public class EditorEntryPoint implements EntryPoint {

  @Override
  public void onModuleLoad() {
    Panel root = RootPanel.get();
    EditorWidget widget = new EditorWidget();
    root.add(widget);
    final FileAgent agent = new FileAgent("http://localhost:8000");
    agent.attach().onResolved(new Callback<Object>() {
      @Override
      public void onSuccess(Object value) {
        agent.getRoot().getFileList().onResolved(new Callback<JavaScriptObject>() {
          @Override
          public void onSuccess(JavaScriptObject value) {
            Console.log(value);
          }
        });
      }
    });
  }

}
