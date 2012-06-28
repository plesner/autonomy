package org.au.tonomy.client.world;

import org.au.tonomy.client.webgl.util.IRenderingFunction;
import org.au.tonomy.client.webgl.util.IWebGL;
import org.au.tonomy.client.webgl.util.Mat4;
import org.au.tonomy.client.webgl.util.Vec4;
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
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
/**
 * A widget that controls the canvas where the game is rendered.
 */
public class WorldWidget extends Composite implements IWorldWidget<Vec4, Mat4> {

  @UiField Canvas canvas;
  @UiField Label fps;
  @UiField Label load;
  @UiField Label log;

  private final IWebGL webGlUtils;
  private final FrameRateMonitor frameRate = new FrameRateMonitor(30);
  private final WorldRenderer renderer;
  private boolean keepRunning = false;
  private IListener listener = null;

  public WorldWidget(IWebGL webGlUtils, Viewport<Vec4, Mat4> viewport, World world) {
    initWidget(BINDER.createAndBindUi(this));
    this.webGlUtils = webGlUtils;
    this.renderer = new WorldRenderer(webGlUtils, canvas, world, viewport, log);
    configureEvents();
  }

  private void configureEvents() {
    canvas.addDomHandler(new MouseDownHandler() {
      @Override
      public void onMouseDown(MouseDownEvent event) {
        getListener().onMouseDown(event.getX(), event.getY());
      }
    }, MouseDownEvent.getType());
    canvas.addDomHandler(new MouseMoveHandler() {
      @Override
      public void onMouseMove(MouseMoveEvent event) {
        getListener().onMouseMove(event.getX(), event.getY());
      }
    }, MouseMoveEvent.getType());
    canvas.addDomHandler(new MouseUpHandler() {
      @Override
      public void onMouseUp(MouseUpEvent event) {
        getListener().onMouseUp();
      }
    }, MouseUpEvent.getType());
    canvas.addDomHandler(new MouseWheelHandler() {
      @Override
      public void onMouseWheel(MouseWheelEvent event) {
        getListener().onMouseWheel(event.getDeltaY());
      }
    }, MouseWheelEvent.getType());
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
    webGlUtils.requestAnimFrame(canvas, new IRenderingFunction() {
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
    canvas.addStyleName(RESOURCES.css().drag());
    canvas.removeStyleName(RESOURCES.css().nodrag());
  }

  @Override
  public void hideDragCursor() {
    canvas.addStyleName(RESOURCES.css().nodrag());
    canvas.removeStyleName(RESOURCES.css().drag());
  }

  public void stopCallingAtFrameRate() {
    keepRunning = false;
  }

  private static String toString(double value) {
    return Double.toString(Math.floor(value * 10 + 0.5) / 10);
  }

  public void refresh() {
    long startMs = System.currentTimeMillis();
    renderer.paint();
    long durationMs = System.currentTimeMillis() - startMs;
    frameRate.record(startMs, durationMs);
    if (frameRate.hasData()) {
      fps.setText(toString(frameRate.getFps()));
      load.setText(toString(frameRate.getLoad() * 100) + "%");
    }
  }

  @UiFactory
  public Canvas createCanvas() {
    return Canvas.createIfSupported();
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
