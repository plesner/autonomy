package org.au.tonomy.shared.world;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates the state of the world.
 */
public class World {

  private final HexGrid grid;
  private final List<Unit> units = new ArrayList<Unit>();

  public World(int width, int height) {
    this.grid = new HexGrid(width, height);
    this.units.add(new Unit(this, 1, 1));
  }

  public HexGrid getGrid() {
    return this.grid;
  }

  public List<Unit> getUnits() {
    return this.units;
  }

  public int getHexWidth() {
    return grid.getHexWidth();
  }

  public int getHexHeight() {
    return grid.getHexHeight();
  }

}
