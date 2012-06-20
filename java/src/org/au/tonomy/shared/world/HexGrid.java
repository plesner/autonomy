package org.au.tonomy.shared.world;

import java.util.Iterator;


/**
 * Helper abstraction for working with a grid of hexes. It maintains a
 * set of hexes and a mapping from hex (g, h) coordinates to normalized
 * cartesian (x, y) coordinates.
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

}
