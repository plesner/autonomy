package org.au.tonomy.client;

import java.util.List;
import java.util.Map;

import org.au.tonomy.client.agent.DocumentHandle;
import org.au.tonomy.client.agent.FileAgent;
import org.au.tonomy.client.agent.FileHandle;
import org.au.tonomy.client.bus.Bus;
import org.au.tonomy.client.bus.DefaultBus;
import org.au.tonomy.client.control.Control;
import org.au.tonomy.client.presentation.EditorPresenter;
import org.au.tonomy.client.presentation.IWorldWidget;
import org.au.tonomy.client.presentation.Viewport;
import org.au.tonomy.client.presentation.WorldPresenter;
import org.au.tonomy.client.util.Callback;
import org.au.tonomy.client.webgl.util.IWebGL;
import org.au.tonomy.client.webgl.util.Mat4;
import org.au.tonomy.client.webgl.util.Vec4;
import org.au.tonomy.client.webgl.util.WebGL;
import org.au.tonomy.client.widget.NotSupportedWidget;
import org.au.tonomy.client.widget.RenderColorScheme;
import org.au.tonomy.client.widget.workspace.WorkspaceWidget;
import org.au.tonomy.shared.util.IFunction;
import org.au.tonomy.shared.util.Promise;
import org.au.tonomy.shared.world.World;
import org.au.tonomy.shared.world.WorldSnapshot;
import org.au.tonomy.shared.world.WorldTrace;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;

public class MainEntryPoint implements EntryPoint {

  private WorkspaceWidget workspace;
  private EditorPresenter editor;

  @Override
  public void onModuleLoad() {
    IWebGL webGl = WebGL.get();
    Panel root = RootPanel.get();
    if (!isSupported(webGl)) {
      root.add(new NotSupportedWidget());
    } else {
      startMain(webGl, root);
    }
  }

  private static boolean isSupported(IWebGL webGlUtils) {
    return Canvas.isSupported() && webGlUtils.isSupported();
  }

  private void buildWorkspace(final Panel root) {
    this.workspace = new WorkspaceWidget();
    this.editor = new EditorPresenter(workspace.getEditor());
    // Build the workspace.
    Scheduler.get().scheduleDeferred(new ScheduledCommand() {
      @Override
      public void execute() {
        root.add(workspace);
      }
    });
  }

  private void startMain(IWebGL webGl, Panel root) {
    final Bus bus = new DefaultBus();
    Bus.set(bus);
    buildWorkspace(root);
    buildEditor();
    buildWorld(webGl);
  }

  private void buildWorld(IWebGL webGl) {
    Console.log(RenderColorScheme.get().getTileColor().getVector());

    if (Window.Location.getParameter("no3d") != null) {
      return;
    }
    World world = new World(32, 32);
    WorldSnapshot initial = new WorldSnapshot(world, 0);
    initial.spawnUnit(16, 16);
    WorldTrace trace = new WorldTrace(world, Control.load(), initial);
    Viewport<Vec4, Mat4> viewport = new Viewport<Vec4, Mat4>(world.getGrid());
    IWorldWidget<Vec4, Mat4> widget = workspace.getWorld();
    widget.setup(webGl, viewport, trace);
    viewport.setCamera(widget.getCamera());
    WorldPresenter<Vec4, Mat4> presenter = new WorldPresenter<Vec4, Mat4>(
        viewport, widget);
    presenter.startAnimating();
  }

  private void buildEditor() {
    final String agentUrl = "localhost:8040";
    // Fetch the files.
    final FileAgent agent = new FileAgent("http://" + agentUrl);
    Promise<DocumentHandle> source = agent
        .attach()
        .lazyThen(new IFunction<Object, Promise<List<FileHandle>>>() {
          @Override
          public Promise<List<FileHandle>> call(Object arg) {
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
