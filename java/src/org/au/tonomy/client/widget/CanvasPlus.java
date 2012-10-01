package org.au.tonomy.client.widget;

import java.util.List;

import org.au.tonomy.shared.util.Assert;
import org.au.tonomy.shared.util.Factory;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
/**
 * A canvas with some additional functionality, including resizing.
 * Don't create too many of these since they're somewhat expensive.
 */
public class CanvasPlus extends Composite {

  private static ICanvasPlusUiBinder BINDER = GWT.create(ICanvasPlusUiBinder.class);
  interface ICanvasPlusUiBinder extends UiBinder<Widget, CanvasPlus> { }

  /**
   * Interface for receiving notifications when this canvas is resized.
   */
  public interface IResizeListener {

    /**
     * Called before the canvas has been resized.
     */
    public void onBeforeResize(int width, int height);

    /**
     * Called after the canvas has been resized.
     */
    public void onAfterResize(int width, int height);

  }

  @UiField FlowPanel container;
  @UiField Canvas canvas;
  private int currentWidth = 0;
  private int currentHeight = 0;
  private final List<IResizeListener> listeners = Factory.newArrayList();
  private HandlerRegistration resizeHandlerRegistration;

  public CanvasPlus() {
    initWidget(BINDER.createAndBindUi(this));
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    registerResizeHandler();
    scheduleRefresh();
  }

  @Override
  protected void onUnload() {
    unregisterResizeHandler();
    super.onUnload();
  }

  private void scheduleRefresh() {
    Scheduler.get().scheduleDeferred(new ScheduledCommand() {
      @Override
      public void execute() {
        refreshLayout();
      }
    });
  }

  /**
   * Registers a resize listener which will take care of resizing the
   * canvas when the window is resized.
   */
  private void registerResizeHandler() {
    Assert.isNull(resizeHandlerRegistration);
    resizeHandlerRegistration = Window.addResizeHandler(new ResizeHandler() {
      @Override
      public void onResize(ResizeEvent event) {
        scheduleRefresh();
      }
    });
  }

  /**
   * Unregisters any registered handlers.
   */
  private void unregisterResizeHandler() {
    if (resizeHandlerRegistration != null) {
      resizeHandlerRegistration.removeHandler();
      resizeHandlerRegistration = null;
    }
  }

  public void addResizeListener(IResizeListener listener) {
    this.listeners.add(listener);
  }

  public Canvas getCanvas() {
    return canvas;
  }

  @UiFactory
  public static Canvas createCanvas() {
    return Canvas.createIfSupported();
  }

  private void refreshLayout() {
    int newWidth = container.getOffsetWidth();
    int newHeight = container.getOffsetHeight();
    if (newWidth == currentWidth && newHeight == currentHeight)
      return;
    for (IResizeListener listener : listeners)
      listener.onBeforeResize(newWidth, newHeight);
    if (newWidth != currentWidth) {
      canvas.setCoordinateSpaceWidth(newWidth);
      canvas.getElement().getStyle().setRight(newWidth, Unit.PX);
      currentWidth = newWidth;
    }
    if (newHeight != currentHeight) {
      canvas.setCoordinateSpaceHeight(newHeight);
      canvas.getElement().getStyle().setBottom(newHeight, Unit.PX);
      currentHeight = newHeight;
    }
    for (IResizeListener listener : listeners)
      listener.onAfterResize(newWidth, newHeight);
  }

}
