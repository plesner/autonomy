package org.au.tonomy.client;

import java.util.Map;

import org.au.tonomy.client.fileagent.FileAgent;
import org.au.tonomy.client.fileagent.FileHandle;
import org.au.tonomy.client.util.Callback;
import org.au.tonomy.client.widget.EditorWidget;
import org.au.tonomy.client.widget.MessagesWidget;
import org.au.tonomy.client.widget.workspace.WorkspaceWidget;
import org.au.tonomy.shared.util.IFunction;
import org.au.tonomy.shared.util.Promise;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.ui.RootPanel;

public class EditorEntryPoint implements EntryPoint {

  private EditorWidget editor;

  private WorkspaceWidget buildWorkspace() {
    WorkspaceWidget workspace = new WorkspaceWidget();
    this.editor = new EditorWidget();
    workspace.setBackground(editor);
    MessagesWidget messages = new MessagesWidget();
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
    bus.setStatus("Connecting to " + agentUrl);
    // Fetch the files.
    final FileAgent agent = new FileAgent("http://" + agentUrl);
    Promise<String> source = agent
        .attach()
        .lazyThen(new IFunction<Object, Promise<Map<String, FileHandle>>>() {
          @Override
          public Promise<Map<String, FileHandle>> call(Object arg) {
            bus.setStatus("Connected to file agent");
            return agent.getRoot().listEntries();
          }
        })
        .lazyThen(new IFunction<Map<String, FileHandle>, Promise<String>>() {
          @Override
          public Promise<String> call(Map<String, FileHandle> files) {
            return files.get("lambdas.aut").readFile();
          }
        });
    source.onResolved(new Callback<String>() {
      @Override
      public void onSuccess(String value) {
        editor.setContents(value);
      }
    });
  }

}
