package org.au.tonomy.client.world;

/**
 * Helper that keeps track of the state associated with dragging the
 * world view.
 */
public class NavigationHelper {

  private final WorldRenderer renderer;

  private int lastDraggedX;
  private int lastDraggedY;
  private boolean isDragging = false;

  public NavigationHelper(WorldRenderer renderer) {
    this.renderer = renderer;
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
    int dX = x - lastDraggedX;
    int dY = lastDraggedY - y;
    lastDraggedX = x;
    lastDraggedY = y;
    renderer.getView().move(dX / 5.0, dY / 5.0);
  }

  public void zoom(int dZ) {
    renderer.getView().zoom(dZ / 10.0);
  }

}
