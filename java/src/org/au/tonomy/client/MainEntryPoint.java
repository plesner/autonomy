package org.au.tonomy.client;

import org.au.tonomy.client.presentation.Viewport;
import org.au.tonomy.client.presentation.WorldPresenter;
import org.au.tonomy.client.webgl.util.IWebGL;
import org.au.tonomy.client.webgl.util.Mat4;
import org.au.tonomy.client.webgl.util.Vec4;
import org.au.tonomy.client.webgl.util.WebGL;
import org.au.tonomy.client.widget.MainWidget;
import org.au.tonomy.client.widget.NotSupportedWidget;
import org.au.tonomy.client.widget.WorldWidget;
import org.au.tonomy.shared.world.World;
import org.au.tonomy.shared.world.WorldSnapshot;
import org.au.tonomy.shared.world.WorldTrace;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;

public class MainEntryPoint implements EntryPoint {

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

  private void startMain(IWebGL webGl, Panel root) {
    World world = new World(32, 32);
    WorldSnapshot initial = new WorldSnapshot(world);
    initial.spawnUnit(4, 4);
    WorldTrace trace = new WorldTrace(world, initial);
    Viewport<Vec4, Mat4> viewport = new Viewport<Vec4, Mat4>(world.getGrid());
    WorldWidget widget = new WorldWidget(webGl, viewport, trace);
    viewport.setCamera(widget.getCamera());
    root.add(new MainWidget(widget));
    WorldPresenter<Vec4, Mat4> presenter = new WorldPresenter<Vec4, Mat4>(
        viewport, widget);
    presenter.startAnimating();
  }

  private static boolean isSupported(IWebGL webGlUtils) {
    return Canvas.isSupported() && webGlUtils.isSupported();
  }

}
