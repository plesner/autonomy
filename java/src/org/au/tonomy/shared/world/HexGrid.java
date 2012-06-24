package org.au.tonomy.shared.world;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.au.tonomy.shared.world.Hex.Side;


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
 * Under this rectangular view an alternative assignment of rectangular
 * hex coordinates can be used:
 *
 * <pre>
 *       / \ / \ / \
 *      |2,2|4,2|0,2|
 *     / \ / \ / \ /
 *    |1,1|3,1|2,1|
 *   / \ / \ / \ /
 *  |0,0|2,0|4,0|
 *   \ / \ / \ /
 * </pre>
 *
 * Each fixed rectangular g or h define a strip of hexes that run in
 * a straight vertical or horizontal line. This view is used when
 * drawing the hexes that lie under a particular rectangular viewport.
 * Any horizontal or vertical line intersects one or two strips. When
 * there are two strips they are known as the upper/lower strip and
 * the leftmost/rightmost strip when there is only one the two types
 * of strips are still well-defined, they're just equal.
 *
 * Most of the tricks used here are described in
 * http://playtechs.blogspot.com.au/2007/04/hex-grids.html
 */
public class HexGrid implements Iterable<Hex> {

  private final int width;
  private final int height;
  private final Hex[][] hexes;
  private final Hex[][] rectHexes;

  public HexGrid(int width, int height) {
    this.width = width;
    this.height = height;
    this.hexes = new Hex[width][height];
    for (int g = 0; g < width; g++) {
      for (int h = 0; h < height; h++) {
        hexes[g][h] = new Hex(g, h);
      }
    }
    int rectWidth = 2 * width;
    int minusOne = rectWidth - 1;
    this.rectHexes = new Hex[rectWidth][height];
    for (int g = 0; g < width; g++) {
      for (int h = 0; h < height; h++) {
        int rectG = getRectG(g, h);
        rectHexes[rectG][h] = hexes[g][h];
        rectHexes[(rectG + minusOne) % rectWidth][h] = hexes[g][h];
      }
    }
  }

  /**
   * Returns the width of a bounding rectangule containing this whole
   * grid.
   */
  public double getBoundingWidth() {
    return (Hex.INNER_DIAMETER * width) + (Hex.INNER_RADIUS * height);
  }

  /**
   * Returns the height of a bounding rectangle containing this whole
   * grid.
   * @return
   */
  public double getBoundingHeight() {
    return (height * 1.5) + 1.5;
  }

  /**
   * Returns the rectangular hex g coordinate for the hex in this grid
   * at the given hex coordinates.
   */
  public int getRectG(int g, int h) {
    return (2 * g + h) % (2 * width);
  }

  public Hex getNeighbour(Hex hex, Hex.Side direction) {
    int newG = (hex.getG() + width + direction.getDeltaG()) % width;
    int newH = (hex.getH() + width + direction.getDeltaH()) % width;
    return hexes[newG][newH];
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

  public Iterable<Hex> getHexes(Viewport viewport) {
    int rectWidth = 2 * width;
    List<Hex> result = new ArrayList<Hex>();
    int rgStart = getLeftmostRectStrip(viewport.getLeft());
    int rgLimit = (getRightmostRectStrip(viewport.getRight()) + 1) % rectWidth;
    int hStart = getLowerRectStrip(viewport.getBottom());
    int hLimit = (getUpperRectStrip(viewport.getTop()) + 1) % height;
    for (int h = hStart, i = 0; (i == 0) || (h != hLimit); h = (h + 1) % rectWidth, i++) {
      Hex boundary = rectHexes[rgLimit][h];
      Hex current = rectHexes[rgStart][h];
      int j = 0;
      while (j == 0 || current != boundary) {
        result.add(current);
        current = getNeighbour(current, Side.EAST);
        j++;
      }
    }
    return result;
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

  /**
   * Returns the lower strip in the rectangular view of the hexes. Only
   * valid for positive inputs.
   */
  public static int getLowerRectStrip(double y) {
    return ((int) Math.floor((y + .5) / .5)) / 3;
  }

  /**
   * Returns the upper strip in the rectangular view of the hexes. Only
   * valid for positive inputs.
   */
  public static int getUpperRectStrip(double y) {
    return ((int) Math.floor((y + 1.0) / .5)) / 3;
  }

  /**
   * Returns the leftmost strip in the rectangular view of the hexes.
   * Only valid for positive inputs.
   */
  public static int getLeftmostRectStrip(double x) {
    return (int) Math.floor(x / Hex.INNER_RADIUS);
  }

  /**
   * Returns the right strip in the rectangular view of the hexes.
   * Only valid for positive inputs.
   */
  public static int getRightmostRectStrip(double x) {
    return getLeftmostRectStrip(x) + 1;
  }

}
