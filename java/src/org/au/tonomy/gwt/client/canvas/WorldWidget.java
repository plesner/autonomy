package org.au.tonomy.gwt.client.canvas;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
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
  private WorldRenderer renderer;

  private boolean isDragging = false;
  private int lastDraggedX = 0;
  private int lastDraggedY = 0;

  public WorldWidget() {
    initWidget(BINDER.createAndBindUi(this));
    renderer = new WorldRenderer(canvas);
    renderer.repaint();
    addDomHandler(new MouseDownHandler() {
      @Override
      public void onMouseDown(MouseDownEvent event) {
        handleMouseDown(event);
      }
    }, MouseDownEvent.getType());
    addDomHandler(new MouseUpHandler() {
      @Override
      public void onMouseUp(MouseUpEvent event) {
        stopDragging();
      }
    }, MouseUpEvent.getType());
    addDomHandler(new MouseMoveHandler() {
      @Override
      public void onMouseMove(MouseMoveEvent event) {
        handleMouseMove(event);
      }
    }, MouseMoveEvent.getType());
    addDomHandler(new MouseOutHandler() {
      @Override
      public void onMouseOut(MouseOutEvent event) {
        stopDragging();
      }
    }, MouseOutEvent.getType());
  }

  private void handleMouseDown(MouseDownEvent event) {
    if (isDragging)
      return;
    isDragging = true;
    lastDraggedX = event.getClientX();
    lastDraggedY = event.getClientY();
  }

  private void stopDragging() {
    if (!isDragging)
      return;
    isDragging = false;
  }

  private void handleMouseMove(MouseMoveEvent event) {
    if (!isDragging)
      return;
    int newX = event.getClientX();
    int newY = event.getClientY();
    int deltaX = newX - lastDraggedX;
    int deltaY = newY - lastDraggedY;
    lastDraggedX = newX;
    lastDraggedY = newY;
    renderer.move(deltaX, deltaY);
    renderer.repaint();
  }

  @UiFactory
  public Canvas createCanvas() {
    return Canvas.createIfSupported();
  }

}
