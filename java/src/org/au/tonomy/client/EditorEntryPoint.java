package org.au.tonomy.client;

import org.au.tonomy.client.filesystem.LocalFile;
import org.au.tonomy.client.util.Callback;
import org.au.tonomy.client.widget.EditorWidget;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;

public class EditorEntryPoint implements EntryPoint {

  private static final String FILE = "/Users/plesner/Documents/autonomy/java/test/org/au/tonomy/shared/syntax/testdata/lambdas.aut";

  @Override
  public void onModuleLoad() {
    Panel root = RootPanel.get();
    final EditorWidget widget = new EditorWidget();
    root.add(widget);
    LocalFile file = LocalFile.forPath(FILE);
    file.getContents().onResolved(new Callback<String>() {
      @Override
      public void onSuccess(String source) {
        widget.setContents(source);
      }
    });
  }

}
