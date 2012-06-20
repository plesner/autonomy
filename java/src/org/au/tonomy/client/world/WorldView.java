package org.au.tonomy.client.world;
/**
 * A collection of the parameters that control how the world renderer
 * displays the world.
 */
public class WorldView {

  private double centerX = 0;
  private double centerY = 0;
  private double zoom = 1;

  public void move(double dX, double dY) {
    this.centerX += dX;
    this.centerY += dY;
  }

  public void zoom(double dZ) {
    this.zoom -= dZ;
  }

  public double getCenterX() {
    return centerX;
  }

  public double getCenterY() {
    return centerY;
  }

  public double getZoom() {
    return zoom;
  }

}
