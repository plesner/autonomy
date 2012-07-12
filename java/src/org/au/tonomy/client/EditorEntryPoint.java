package org.au.tonomy.client;

import org.au.tonomy.client.local.LocalFileService;
import org.au.tonomy.client.presentation.CodeEditorPresenter;
import org.au.tonomy.client.widget.EditorWidget;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;

public class EditorEntryPoint implements EntryPoint {

  private static final String FILE = "/Users/plesner/Documents/autonomy/java/test/org/au/tonomy/shared/syntax/testdata/lambdas.aut";

  @Override
  public void onModuleLoad() {
    Panel root = RootPanel.get();
    EditorWidget widget = new EditorWidget();
    root.add(widget);
    final CodeEditorPresenter presenter = new CodeEditorPresenter(widget);
    LocalFileService.getContents(FILE, new AsyncCallback<String>() {
      @Override
      public void onSuccess(String result) {
        presenter.getSourceManager().setSource(result);
      }
      @Override
      public void onFailure(Throwable caught) {
        Window.alert(caught.toString());
      }
    });

  }

}
