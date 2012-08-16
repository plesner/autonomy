package org.au.tonomy.client;

import java.util.List;

import org.au.tonomy.client.fileagent.FileAgent;
import org.au.tonomy.client.fileagent.FileHandle;
import org.au.tonomy.client.util.Callback;
import org.au.tonomy.client.widget.EditorWidget;
import org.au.tonomy.shared.util.IFunction;
import org.au.tonomy.shared.util.Promise;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;

public class EditorEntryPoint implements EntryPoint {

  @Override
  public void onModuleLoad() {
    Panel root = RootPanel.get();
    final EditorWidget widget = new EditorWidget();
    root.add(widget);
    final FileAgent agent = new FileAgent("http://localhost:8000");
    Promise<String> source = agent
        .attach()
        .lazyThen(new IFunction<Object, Promise<List<FileHandle>>>() {
          @Override
          public Promise<List<FileHandle>> call(Object arg) {
            return agent.getRoot().getFileList();
          }
        })
        .lazyThen(new IFunction<List<FileHandle>, Promise<String>>() {
          @Override
          public Promise<String> call(List<FileHandle> handles) {
            return handles.get(0).read();
          }
        });
    source.onResolved(new Callback<String>() {
      @Override
      public void onSuccess(String value) {
        widget.setContents(value);
      }
    });
  }

}
