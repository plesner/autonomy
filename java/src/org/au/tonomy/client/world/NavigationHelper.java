package org.au.tonomy.client.world;

import org.au.tonomy.shared.world.Viewport;

/**
 * Helper that keeps track of the state associated with dragging the
 * world view.
 */
public class NavigationHelper {

  private final Viewport viewport;

  private int lastDraggedX;
  private int lastDraggedY;
  private boolean isDragging = false;

  public NavigationHelper(Viewport viewport) {
    this.viewport = viewport;
  }

  public void startDragging(int x, int y) {
    if (isDragging)
      return;
    this.lastDraggedX = x;
    this.lastDraggedY = y;
    this.isDragging = true;
  }

  public void stopDragging() {
    this.isDragging = false;
  }

  public void drag(int x, int y) {
    if (!isDragging)
      return;
    int dX = lastDraggedX - x;
    int dY = y - lastDraggedY;
    lastDraggedX = x;
    lastDraggedY = y;
    viewport.move(dX / 8.0, dY / 8.0);
  }

  public void zoom(int dZ) {
    // renderer.getView().zoom(dZ / 10.0);
  }

}
