package org.au.tonomy.client.world;

import org.au.tonomy.client.webgl.util.RenderingFunction;
import org.au.tonomy.client.webgl.util.WebGLUtils;
import org.au.tonomy.shared.world.Viewport;
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
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
/**
 * A widget that controls the canvas where the game is rendered.
 */
public class WorldWidget extends Composite {

  private static final WorldWidgetUiBinder BINDER = GWT.create(WorldWidgetUiBinder.class);
  interface WorldWidgetUiBinder extends UiBinder<Widget, WorldWidget> { }

  @UiField Canvas canvas;
  @UiField Label fps;
  @UiField Label load;

  private final Viewport viewport = new Viewport(1.5, 0.25, 5.25, 4.0);
  private final FrameRateMonitor frameRate = new FrameRateMonitor(30);
  private final WorldRenderer renderer;
  private final World world;
  private final NavigationHelper navigation;
  private boolean keepRunning = false;

  public WorldWidget(World world) {
    initWidget(BINDER.createAndBindUi(this));
    this.world = world;
    this.renderer = new WorldRenderer(canvas, world, viewport);
    this.renderer.paint();
    this.navigation = new NavigationHelper(viewport);
    setUpDragging();
  }

  public void start() {
    if (keepRunning)
      return;
    keepRunning = true;
    WebGLUtils.requestAnimFrame(canvas, new RenderingFunction() {
      @Override
      public void tick() {
        refresh();
      }
      @Override
      public boolean shouldContinue() {
        return keepRunning;
      }
    });
  }

  private static String toString(double value) {
    return Double.toString(Math.floor(value * 10 + 0.5) / 10);
  }

  private void refresh() {
    long startMs = System.currentTimeMillis();
    renderer.paint();
    long durationMs = System.currentTimeMillis() - startMs;
    frameRate.record(startMs, durationMs);
    if (frameRate.hasData()) {
      fps.setText(toString(frameRate.getFps()));
      load.setText(toString(frameRate.getLoad() * 100) + "%");
    }
  }

  private void setUpDragging() {
    this.addDomHandler(new MouseDownHandler() {
      @Override
      public void onMouseDown(MouseDownEvent event) {
        navigation.startDragging(event.getX(), event.getY());
      }
    }, MouseDownEvent.getType());
    this.addDomHandler(new MouseMoveHandler() {
      @Override
      public void onMouseMove(MouseMoveEvent event) {
        navigation.drag(event.getX(), event.getY());
      }
    }, MouseMoveEvent.getType());
    this.addDomHandler(new MouseUpHandler() {
      @Override
      public void onMouseUp(MouseUpEvent event) {
        navigation.stopDragging();
      }
    }, MouseUpEvent.getType());
    this.addDomHandler(new MouseWheelHandler() {
      @Override
      public void onMouseWheel(MouseWheelEvent event) {
        navigation.zoom(event.getDeltaY());
      }
    }, MouseWheelEvent.getType());
  }

  @UiFactory
  public Canvas createCanvas() {
    return Canvas.createIfSupported();
  }

}
