package org.au.tonomy.client.world;

import org.au.tonomy.shared.world.HexGrid;

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
import com.google.gwt.user.client.ui.Widget;
/**
 * A widget that controls the canvas where the game is rendered.
 */
public class WorldWidget extends Composite {

  private static final WorldWidgetUiBinder BINDER = GWT.create(WorldWidgetUiBinder.class);
  interface WorldWidgetUiBinder extends UiBinder<Widget, WorldWidget> { }

  @UiField Canvas canvas;
  private final WorldRenderer renderer;
  private final HexGrid grid = new HexGrid(4, 4);
  private final NavigationHelper navigation;

  public WorldWidget() {
    initWidget(BINDER.createAndBindUi(this));
    this.renderer = new WorldRenderer(canvas, grid);
    this.renderer.paint();
    this.navigation = new NavigationHelper(renderer);
    setUpDragging();
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
