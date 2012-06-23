package org.au.tonomy.shared.world;

import java.util.Iterator;


/**
 * Helper abstraction for working with a grid of hexes. It maintains a
 * set of hexes and a mapping from hex (g, h) coordinates to normalized
 * cartesian (x, y) coordinates.
 *
 * The hexes are ordered in a coordinate system like so:
 *
 * <pre>
 *       / \ / \ / \
 *      |0,2|1,2|2,2|
 *     / \ / \ / \ /
 *    |0,1|1,1|2,1|
 *   / \ / \ / \ /
 *  |0,0|1,0|2,0|
 *   \ / \ / \ /
 * </pre>
 *
 * The grid is on a torus and wraps around. So square views of the
 * grid may have to wrap cells around:
 *
 * <pre>
 *   / \ / \ / \
 *  |2,2|0,2|1,2|
 *   \ / \ / \ / \
 *    |0,1|1,1|2,1|
 *   / \ / \ / \ /
 *  |0,0|1,0|2,0|
 *   \ / \ / \ /
 * </pre>
 *
 * Most of the tricks used here are described in
 * http://playtechs.blogspot.com.au/2007/04/hex-grids.html
 */
public class HexGrid implements Iterable<Hex> {

  private final int width;
  private final int height;
  private final Hex[][] hexes;

  public HexGrid(int width, int height) {
    this.width = width;
    this.height = height;
    this.hexes = new Hex[width][height];
    for (int g = 0; g < width; g++) {
      for (int h = 0; h < height; h++) {
        hexes[g][h] = new Hex(g, h);
      }
    }
  }

  /**
   * Returns the g component of the hex coordinates of the hex in the
   * given direction of one with the given g component.
   */
  public int getMovedG(int g, Hex.Side direction) {
    return clip(g + direction.getDeltaG(), width);
  }

  /**
   * Returns the h component of the hex coordinates of the hex in the
   * given direction of one with the given h component.
   */
  public int getMovedH(int h, Hex.Side direction) {
    return clip(h + direction.getDeltaH(), height);
  }

  public Iterator<Hex> iterator() {
    return new Iterator<Hex>() {
      private int g = 0;
      private int h = 0;
      @Override
      public boolean hasNext() {
        return g < width && h < height;
      }
      @Override
      public Hex next() {
        Hex result = hexes[g][h];
        if (++g == width) {
          g = 0;
          h++;
        }
        return result;
      }
      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }

  /**
   * Returns a value that is equal to v modulo max and is between 0
   * and max.
   */
  public static int clip(int v, int max) {
    // TODO: I'm sure this could be done in a smarter way.
    if (v < 0) {
      return ((v % max) + max) % max;
    } else if (v >= max) {
      return v % max;
    } else {
      return v;
    }
  }

  /**
   * Returns the unit diagonal this unit coordinates belong to. A unit
   * diagonal is a strip of blocks 3 units wide that move diagonally
   * across, like so (if each character is a 1x1 unit square):
   *
   * <pre>
   *  ***...***...**
   *  .***...***...*
   *  ..***...***...
   *  ...***...***..
   *  *...***...***.
   *  **...***...***
   *  ***...***...**
   * <pre>
   */
  public static int getUnitDiagonal(int tx, int ty) {
    return (ty + tx + 2) / 3;
  }

  private static final double INVERSE_INNER_DIAMETER = 1 / Hex.INNER_DIAMETER;
  private static final double INVERSE_INNER_RADIUS = 1 / Hex.INNER_RADIUS;

  /**
   * Returns the unit x coordinate for the hex h axis.
   */
  public static double getUnitHX(double x, double y) {
    return INVERSE_INNER_DIAMETER * x + y;
  }

  /**
   * Returns the unit y coordinate for the hex h axis.
   */
  public static double getUnitHY(double x, double y) {
    return -INVERSE_INNER_DIAMETER * x + y;
  }

  /**
   * Returns the unit x coordinate for the hex g axis.
   */
  public static double getUnitGX(double x, double y) {
    return INVERSE_INNER_RADIUS * x;
  }

  /**
   * Returns the unit y coordinate for the hex g axis.
   */
  public static double getUnitGY(double x, double y) {
    return INVERSE_INNER_DIAMETER * x - y;
  }

  /**
   * Determines the hex coordinates of the hex that lies under the
   * given normalized cartesian coordinates. It does this by converting
   * the point to unit coordinates twice, once in a way that makes the
   * g axis line up diagonally and once in a way that makes the h axis
   * line up, and for each it determines which diagonal it's on.
   */
  public void locate(double x, double y, HexPoint point) {
    double uGX = Math.floor(getUnitGX(x, y));
    double uGY = Math.floor(getUnitGY(x, y));
    int g = getUnitDiagonal((int) uGX, (int) uGY);
    double uHX = Math.floor(getUnitHX(x, y));
    double uHY = Math.floor(getUnitHY(x, y));
    int h = getUnitDiagonal((int) uHX, (int) uHY);
    point.set(g, h);
  }

  /**
   * Returns the hex with the given coordinates.
   */
  public Hex getHex(int g, int h) {
    return hexes[g][h];
  }

}
