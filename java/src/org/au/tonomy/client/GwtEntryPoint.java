package org.au.tonomy.client;

import org.au.tonomy.client.webgl.util.IWebGL;
import org.au.tonomy.client.webgl.util.Mat4;
import org.au.tonomy.client.webgl.util.Vec4;
import org.au.tonomy.client.webgl.util.WebGL;
import org.au.tonomy.client.widget.MainWidget;
import org.au.tonomy.client.widget.NotSupportedWidget;
import org.au.tonomy.client.widget.Viewport;
import org.au.tonomy.client.widget.WorldPresenter;
import org.au.tonomy.client.widget.WorldWidget;
import org.au.tonomy.shared.world.World;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;

public class GwtEntryPoint implements EntryPoint {

  @Override
  public void onModuleLoad() {
    IWebGL webGlUtils = WebGL.get();
    RootPanel root = RootPanel.get();
    if (!isSupported(webGlUtils)) {
      root.add(new NotSupportedWidget());
    } else {
      World world = new World(16, 16);
      Viewport<Vec4, Mat4> viewport = new Viewport<Vec4, Mat4>(world.getGrid());
      WorldWidget widget = new WorldWidget(webGlUtils, viewport, world);
      viewport.setCamera(widget.getCamera());
      root.add(new MainWidget(widget));
      WorldPresenter<Vec4, Mat4> presenter = new WorldPresenter<Vec4, Mat4>(world,
          viewport, widget);
      presenter.startAnimating();
    }
  }

  private static boolean isSupported(IWebGL webGlUtils) {
    return Canvas.isSupported() && webGlUtils.isSupported();
  }

}
