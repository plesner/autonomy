package org.au.tonomy.client.presentation;

import org.au.tonomy.shared.util.IMatrix;
import org.au.tonomy.shared.util.IVector;
import org.au.tonomy.shared.world.World;

/**
 * Presenter class for the world widget.
 */
public class WorldPresenter<V4 extends IVector, M4 extends IMatrix<V4>> implements IWorldWidget.IListener {

  private final IWorldWidget<V4, M4> widget;
  private final Viewport<V4, M4> viewport;

  private boolean isPanning = false;
  private V4 panHandle;

  public WorldPresenter(World world, Viewport<V4, M4> viewport,
      IWorldWidget<V4, M4> widget) {
    this.widget = widget;
    this.viewport = viewport;
    widget.attachListener(this);
  }

  public void startAnimating() {
    widget.callAtFrameRate(new Runnable() {
      @Override
      public void run() {
        tick();
      }
    });
  }

  public void stopAnimating() {
    widget.stopCallingAtFrameRate();
  }

  public void tick() {
    widget.refresh();
  }

  @Override
  public void onMouseDown(int canvasX, int canvasY) {
    if (isPanning)
      return;
    isPanning = true;
    panHandle = viewport.canvasToScene(canvasX, canvasY);
    widget.showDragCursor();
  }

  @Override
  public void onMouseUp() {
    if (!isPanning)
      return;
    isPanning = false;
    widget.hideDragCursor();
  }

  @Override
  public void onMouseMove(int canvasX, int canvasY) {
    if (!isPanning)
      return;
    V4 target = viewport.canvasToScene(canvasX, canvasY);
    double moveX = panHandle.getX() - target.getX();
    double moveY = panHandle.getY() - target.getY();
    viewport.translate(moveX, moveY);
  }

  @Override
  public void onMouseWheel(int deltaY) {
    viewport.zoom(deltaY / 100.0);
  }

  @Override
  public void onBeforeResize(int width, int height) {
    viewport.adjustToCanvasResize(width, height);
  }

}
