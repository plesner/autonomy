package org.au.tonomy.shared.world;

import org.au.tonomy.shared.util.ExtraMath;

/**
 * A single hex in a hex grid. The layout of a hex:
 *
 * <pre>
 *            3
 *
 *       4    N    2
 *          /   \
 *  5 NW  /       \ NE  1
 *      |           |
 *  6   W           E   0
 *      |           |
 *  7 SW  \       / SE  11
 *          \   /
 *       8    S    10
 *
 *            9
 * </pre>
 *
 * The sides are the surrounding lines, the corners are the, well,
 * corners. The numbers indicate the canonical ordering of sides and
 * corners which follows the normal ordering of radians around a circle.
 */
public class Hex {

  /**
   * An enum identifying the sides of a hex.
   */
  public enum Side {

    EAST(1, 0),
    NORTH_EAST(1, -1),
    NORTH_WEST(0, -1),
    WEST(-1, 0),
    SOUTH_WEST(-1, 1),
    SOUTH_EAST(0, 1);

    private final int deltaG;
    private final int deltaH;

    private Side(int deltaG, int deltaH) {
      this.deltaG = deltaG;
      this.deltaH = deltaH;
    }

    public int getDeltaG() {
      return deltaG;
    }

    public int getDeltaH() {
      return deltaH;
    }

  }

  /**
   * Enum identifying the corners of a hex.
   */
  public enum Corner {

    NORTH_EAST(30),
    NORTH(90),
    NORTH_WEST(150),
    SOUTH_WEST(210),
    SOUTH(270),
    SOUTH_EAST(330);

    private final double radians;
    private final double x;
    private final double y;

    private Corner(int degrees) {
      this.radians = degrees * ExtraMath.DEGREES_TO_RADIANS;
      this.x = Math.cos(radians);
      this.y = Math.sin(radians);
    }

    public double getX() {
      return this.x;
    }

    public double getY() {
      return this.y;
    }

  }

  private static final double SHORT = Math.cos(ExtraMath.TAU / 6);
  private static final double LONG = Math.sin(ExtraMath.TAU / 6);
  private static final double SIDE = 2 * SHORT;
  private static final double WIDTH = 2 * LONG;
  private static final double HEIGHT = SIDE + SHORT;

  private final int g;
  private final int h;
  private final double centerX;
  private final double centerY;

  public Hex(int g, int h) {
    this.g = g;
    this.h = h;
    this.centerX = LONG + (WIDTH * g) + (LONG * h);
    this.centerY = LONG + HEIGHT * h;
  }

  /**
   * Returns the first hex coordinate.
   */
  public int getG() {
    return g;
  }

  /**
   * Returns the second hex coordinate.
   */
  public int getH() {
    return h;
  }

  /**
   * Returns the x-coordinate of the center of this hex in the normalized
   * coordinate system.
   */
  public double getCenterX() {
    return this.centerX;
  }

  /**
   * Returns the y-coordinate of the center of this hex in the normalized
   * coordinate system.
   */
  public double getCenterY() {
    return this.centerY;
  }

}
