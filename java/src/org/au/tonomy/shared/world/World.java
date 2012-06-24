package org.au.tonomy.shared.world;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates the state of the world.
 */
public class World {

  private final int width;
  private final int height;
  private final HexGrid grid;
  private final List<Unit> units = new ArrayList<Unit>();

  public World(int width, int height) {
    this.width = width;
    this.height = height;
    this.grid = new HexGrid(width, height);
    this.units.add(new Unit(this, 1, 1));
  }

  public HexGrid getGrid() {
    return this.grid;
  }

  public List<Unit> getUnits() {
    return this.units;
  }

  public int getWidth() {
    return this.width;
  }

  public int getHeight() {
    return this.height;
  }

}
