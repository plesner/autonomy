package org.au.tonomy.client.world;

import org.au.tonomy.shared.util.IMatrix;
import org.au.tonomy.shared.util.IVector;
import org.au.tonomy.shared.world.HexGrid;
import org.au.tonomy.shared.world.HexPoint;
/**
 * This class takes care of updating the renderer's view of the scene
 * based on actions on the widget.
 */
public class Viewport<V4 extends IVector, M4 extends IMatrix<V4>> {

  private final HexGrid grid;
  private ICamera<V4, M4> camera;

  private double left = 0;
  private double right = 10;
  private double bottom = 0;
  private double top = 10;

  public Viewport(HexGrid grid) {
    this.grid = grid;
  }

  public void setCamera(ICamera<V4, M4> camera) {
    this.camera = camera;
  }

  public double getLeft() {
    return this.left;
  }

  public double getRight() {
    return this.right;
  }

  public double getBottom() {
    return this.bottom;
  }

  public double getTop() {
    return this.top;
  }

  public double getWidth() {
    return right - left;
  }

  public double getHeight() {
    return top - bottom;
  }

  /**
   * Maps rect coordinates on the canvas to rect coordinates in the
   * scene.
   */
  public V4 canvasToScene(double canvasX, double canvasY) {
    double canvasWidth = camera.getCanvasWidth();
    double canvasHeight = camera.getCanvasHeight();
    double normalX = (2 * canvasX - canvasWidth) / canvasWidth;
    double normalY = (-2 * canvasY + canvasHeight) / canvasHeight;
    return camera.getInversePerspective().multiply(normalX, normalY, 0, 1);
  }

  /**
   * Converts a pair of canvas coordinates into the hex coordinate of
   * the hex drawn under that location.
   */
  public void canvasToHex(double canvasX, double canvasY, HexPoint pointOut) {
    V4 scenePoint = canvasToScene(canvasX, canvasY);
    double sceneX = scenePoint.get(0);
    double sceneY = scenePoint.get(1);
    grid.rectToHex(sceneX, sceneY, pointOut);
  }

  /**
   * Moves this viewport by the specified amount, specified in scene
   * coordinates.
   */
  public void translate(double dX, double dY) {
    this.left += dX;
    this.right += dX;
    this.top += dY;
    this.bottom += dY;
  }

}
