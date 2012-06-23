package org.au.tonomy.shared.world;

/**
 * Encapsulates the state of the world.
 */
public class World {

  private final HexGrid grid;

  public World(int width, int height) {
    this.grid = new HexGrid(width, height);
  }

}
