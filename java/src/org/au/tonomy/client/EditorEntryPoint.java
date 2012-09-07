package org.au.tonomy.client;

import java.util.List;
import java.util.Map;

import org.au.tonomy.client.agent.DocumentHandle;
import org.au.tonomy.client.agent.FileAgent;
import org.au.tonomy.client.agent.FileHandle;
import org.au.tonomy.client.bus.Bus;
import org.au.tonomy.client.bus.DefaultBus;
import org.au.tonomy.client.bus.Message;
import org.au.tonomy.client.presentation.EditorPresenter;
import org.au.tonomy.client.util.Callback;
import org.au.tonomy.client.widget.EditorWidget;
import org.au.tonomy.client.widget.MessageListWidget;
import org.au.tonomy.client.widget.PageWidget;
import org.au.tonomy.client.widget.workspace.WorkspaceWidget;
import org.au.tonomy.shared.util.IFunction;
import org.au.tonomy.shared.util.Promise;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.ui.RootPanel;

public class EditorEntryPoint implements EntryPoint {

  private PageWidget page;
  private EditorPresenter editor;

  private WorkspaceWidget buildWorkspace() {
    WorkspaceWidget workspace = new WorkspaceWidget();
    PageWidget page = new PageWidget();
    EditorWidget editorWidget = new EditorWidget();
    this.editor = new EditorPresenter(editorWidget);
    page.setContents(editorWidget);
    workspace.setBackground(page);
    MessageListWidget messages = new MessageListWidget();
    workspace.addPanel(messages);
    return workspace;
  }

  @Override
  public void onModuleLoad() {
    final Bus bus = new DefaultBus();
    Bus.set(bus);
    // Build the workspace.
    final WorkspaceWidget workspace = buildWorkspace();
    Scheduler.get().scheduleDeferred(new ScheduledCommand() {
      @Override
      public void execute() {
        RootPanel.get().add(workspace);
      }
    });
    final String agentUrl = "localhost:8040";
    final Message message = new Message("Connecting to " + agentUrl);
    bus.addMessage(message);
    // Fetch the files.
    final FileAgent agent = new FileAgent("http://" + agentUrl);
    Promise<DocumentHandle> source = agent
        .attach()
        .lazyThen(new IFunction<Object, Promise<List<FileHandle>>>() {
          @Override
          public Promise<List<FileHandle>> call(Object arg) {
            message.setText("Connected to file agent on " + agentUrl);
            message.setExpiration(1000);
            return agent.getSession().getRoots();
          }
        })
        .lazyThen(new IFunction<List<FileHandle>, Promise<Map<String, FileHandle>>>() {
          @Override
          public Promise<Map<String, FileHandle>> call(List<FileHandle> roots) {
            return roots.get(0).listEntries();
          }
        })
        .lazyThen(new IFunction<Map<String, FileHandle>, Promise<? extends DocumentHandle>>() {
          @Override
          public Promise<? extends DocumentHandle> call(Map<String, FileHandle> files) {
            return files.get("lambdas.aut").readFile();
          }
        });
    source.onResolved(new Callback<DocumentHandle>() {
      @Override
      public void onSuccess(DocumentHandle value) {
        editor.setContents(value);
      }
    });
  }

}
