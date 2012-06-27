package org.au.tonomy.client;

import org.au.tonomy.client.webgl.util.IWebGLUtils;
import org.au.tonomy.client.webgl.util.Mat4;
import org.au.tonomy.client.webgl.util.Vec4;
import org.au.tonomy.client.webgl.util.WebGLUtils;
import org.au.tonomy.client.world.Viewport;
import org.au.tonomy.client.world.WorldPresenter;
import org.au.tonomy.client.world.WorldWidget;
import org.au.tonomy.shared.world.World;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;

public class GwtEntryPoint implements EntryPoint {

  @Override
  public void onModuleLoad() {
    IWebGLUtils webGlUtils = WebGLUtils.get();
    World world = new World(16, 16);
    Viewport<Vec4, Mat4> viewport = new Viewport<Vec4, Mat4>(world.getGrid());
    WorldWidget widget = new WorldWidget(webGlUtils, viewport, world);
    viewport.setCamera(widget.getCamera());
    RootPanel.get().add(widget);
    WorldPresenter<Vec4, Mat4> presenter = new WorldPresenter<Vec4, Mat4>(world,
        viewport, widget);
    presenter.startAnimating();
  }

}
