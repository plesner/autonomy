package org.au.tonomy.client.widget;

import org.au.tonomy.client.presentation.FrameRateMonitor;
import org.au.tonomy.client.presentation.ICamera;
import org.au.tonomy.client.presentation.IWorldWidget;
import org.au.tonomy.client.presentation.Viewport;
import org.au.tonomy.client.webgl.util.IRenderingFunction;
import org.au.tonomy.client.webgl.util.IWebGL;
import org.au.tonomy.client.webgl.util.Mat4;
import org.au.tonomy.client.webgl.util.Vec4;
import org.au.tonomy.client.widget.CanvasPlus.IResizeListener;
import org.au.tonomy.shared.util.Assert;
import org.au.tonomy.shared.world.World;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
/**
 * A widget that controls the canvas where the game is rendered.
 */
public class WorldWidget extends Composite implements IWorldWidget<Vec4, Mat4> {

  @UiField CanvasPlus canvasWrapper;

  private final IWebGL webGlUtils;
  private final FrameRateMonitor frameRate = new FrameRateMonitor(30);
  private final WorldRenderer renderer;
  private boolean keepRunning = false;
  private IListener listener = null;

  public WorldWidget(IWebGL webGlUtils, Viewport<Vec4, Mat4> viewport, World world) {
    initWidget(BINDER.createAndBindUi(this));
    this.webGlUtils = webGlUtils;
    this.renderer = new WorldRenderer(webGlUtils, getCanvas(), world, viewport);
    configureEvents();
  }

  private Canvas getCanvas() {
    return canvasWrapper.getCanvas();
  }

  private void configureEvents() {
    getCanvas().addDomHandler(new MouseDownHandler() {
      @Override
      public void onMouseDown(MouseDownEvent event) {
        getListener().onMouseDown(event.getX(), event.getY());
      }
    }, MouseDownEvent.getType());
    getCanvas().addDomHandler(new MouseMoveHandler() {
      @Override
      public void onMouseMove(MouseMoveEvent event) {
        getListener().onMouseMove(event.getX(), event.getY());
      }
    }, MouseMoveEvent.getType());
    getCanvas().addDomHandler(new MouseUpHandler() {
      @Override
      public void onMouseUp(MouseUpEvent event) {
        getListener().onMouseUp();
      }
    }, MouseUpEvent.getType());
    getCanvas().addDomHandler(new MouseWheelHandler() {
      @Override
      public void onMouseWheel(MouseWheelEvent event) {
        getListener().onMouseWheel(event.getDeltaY());
      }
    }, MouseWheelEvent.getType());
    canvasWrapper.addResizeListener(new IResizeListener() {
      @Override
      public void onBeforeResize(int width, int height) {
        getListener().onBeforeResize(width, height);
      }
      @Override
      public void onAfterResize(int width, int height) {
        // ignore
      }
    });
  }

  public ICamera<Vec4, Mat4> getCamera() {
    return renderer;
  }

  public void attachListener(IListener listener) {
    Assert.that(this.listener == null);
    this.listener = listener;
  }

  private IListener getListener() {
    return Assert.notNull(this.listener);
  }

  public void callAtFrameRate(final Runnable thunk) {
    if (keepRunning)
      return;
    keepRunning = true;
    webGlUtils.requestAnimFrame(getCanvas(), new IRenderingFunction() {
      @Override
      public void tick() {
        thunk.run();
      }
      @Override
      public boolean shouldContinue() {
        return keepRunning;
      }
    });
  }

  @Override
  public void showDragCursor() {
    getCanvas().addStyleName(RESOURCES.css().drag());
    getCanvas().removeStyleName(RESOURCES.css().nodrag());
  }

  @Override
  public void hideDragCursor() {
    getCanvas().addStyleName(RESOURCES.css().nodrag());
    getCanvas().removeStyleName(RESOURCES.css().drag());
  }

  public void stopCallingAtFrameRate() {
    keepRunning = false;
  }

  public void refresh() {
    long startMs = System.currentTimeMillis();
    renderer.paint();
    long durationMs = System.currentTimeMillis() - startMs;
    frameRate.record(startMs, durationMs);
  }

  private static final IWorldWidgetUiBinder BINDER = GWT.create(IWorldWidgetUiBinder.class);
  interface IWorldWidgetUiBinder extends UiBinder<Widget, WorldWidget> { }

  /**
   * Bindings for the world widget css.
   */
  public interface Css extends CssResource {

    public String drag();

    public String nodrag();

    public String world();

  }

  /**
   * World widget resource bundle.
   */
  public interface Resources extends ClientBundle {

    @Source("WorldWidget.css")
    public Css css();

  }

  public static final Resources RESOURCES = GWT.create(Resources.class);
  static { RESOURCES.css().ensureInjected(); }

}
