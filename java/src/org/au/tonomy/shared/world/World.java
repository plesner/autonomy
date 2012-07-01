package org.au.tonomy.shared.world;

/**
 * The data about a world that doesn't change over time.
 */
public class World {

  private final HexGrid grid;
  private int unitCount = 0;

  public World(int width, int height) {
    this.grid = new HexGrid(width, height);
  }

  public HexGrid getGrid() {
    return this.grid;
  }

  /**
   * Returns a new unit with a unique id.
   */
  public Unit newUnit() {
    return new Unit(unitCount++);
  }

}
