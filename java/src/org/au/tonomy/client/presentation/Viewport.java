package org.au.tonomy.client.presentation;

import org.au.tonomy.shared.util.IMatrix;
import org.au.tonomy.shared.util.IRect;
import org.au.tonomy.shared.util.IVector;
import org.au.tonomy.shared.world.Hex;
import org.au.tonomy.shared.world.HexGrid;
import org.au.tonomy.shared.world.HexPoint;
/**
 * This class takes care of updating the renderer's view of the scene
 * based on actions on the widget.
 */
public class Viewport<V4 extends IVector, M4 extends IMatrix<V4>> {

  private static final double MIN_SIDE = 5;
  private static final double DEFAULT_SIDE = 10;

  private final HexGrid grid;
  private final MutableRect bounds = new MutableRect();
  private ICamera<V4, M4> camera;

  public Viewport(HexGrid grid) {
    this.grid = grid;
    this.resetBounds();
  }

  private void resetBounds() {
    double centerX = grid.getRectWidth() / 2;
    double centerY = grid.getRectHeight() / 2;
    double distX = DEFAULT_SIDE / 2;
    double distY = DEFAULT_SIDE / 2;
    bounds.reset(centerX - distX, centerX + distX, centerY - distY,
        centerY + distY);
    applyRestrictions();
  }

  public void setCamera(ICamera<V4, M4> camera) {
    this.camera = camera;
  }

  /**
   * Returns the viewport bounds in scene coordinates.
   */
  public IRect getBounds() {
    return bounds;
  }

  /**
   * Returns the max viewport width beyond which the same hex may appear
   * more than once within the same view.
   */
  public double getMaxWidth() {
    return (grid.getHexWidth() - 1) * Hex.INNER_DIAMETER;
  }

  /**
   * Returns the max viewport height beyond which the same hex may appear
   * more than once within the same view.
   */
  public double getMaxHeight() {
    return (grid.getHexHeight() * 1.5) - 2;
  }

  public double getCanvasWidth() {
    return camera.getCanvasWidth();
  }

  public double getCanvasHeight() {
    return camera.getCanvasHeight();
  }

  public void zoom(double factor) {
    double deltaX = (bounds.getWidth() * factor) / 2;
    double deltaY = (bounds.getHeight() * factor) / 2;
    bounds.reset(bounds.getLeft() - deltaX, bounds.getRight() + deltaX,
        bounds.getBottom() - deltaY, bounds.getTop() + deltaY);
  }

  private static double getAdjustmentZoom(double target, double current) {
    return (target - current) / current;
  }

  private void applyRestrictions() {
    if (bounds.getWidth() < MIN_SIDE) {
      zoom(getAdjustmentZoom(MIN_SIDE, bounds.getWidth()));
    } else if (bounds.getWidth() > getMaxWidth()) {
      zoom(getAdjustmentZoom(getMaxWidth(), bounds.getWidth()));
    }
    if (bounds.getHeight() < MIN_SIDE) {
      zoom(getAdjustmentZoom(MIN_SIDE, bounds.getHeight()));
    } else if (bounds.getHeight() > getMaxHeight()) {
      zoom(getAdjustmentZoom(getMaxHeight(), bounds.getHeight()));
    }
  }

  /**
   * Maps rect coordinates on the canvas to rect coordinates in the
   * scene.
   */
  public V4 canvasToScene(double canvasX, double canvasY) {
    double canvasWidth = getCanvasWidth();
    double canvasHeight = getCanvasHeight();
    double normalX = (2 * canvasX - canvasWidth) / canvasWidth;
    double normalY = (-2 * canvasY + canvasHeight) / canvasHeight;
    return camera.getInversePerspective().multiply(normalX, normalY, 0, 1);
  }

  /**
   * Converts a pair of canvas coordinates into the hex coordinate of
   * the hex drawn under that location.
   */
  public HexPoint canvasToHex(double canvasX, double canvasY) {
    V4 scenePoint = canvasToScene(canvasX, canvasY);
    double sceneX = scenePoint.getX();
    double sceneY = scenePoint.getY();
    return grid.rectToHex(sceneX, sceneY);
  }

  /**
   * Moves this viewport by the specified amount, specified in scene
   * coordinates.
   */
  public void translate(double dx, double dy) {
    bounds.translate(dx, dy);
  }

  public void adjustToCanvasResize(int width, int height) {
    // ignore for now
  }

}
