package org.au.tonomy.shared.world;
public class Unit {

  private final World world;
  private int g;
  private int h;

  public Unit(World world, int g, int h) {
    this.world = world;
    this.g = g;
    this.h = h;
  }

  public Hex getLocation() {
    return world.getGrid().getHex(g, h);
  }

  public void move(Hex.Side direction) {
    this.g = world.getGrid().getMovedG(g, direction);
    this.h = world.getGrid().getMovedH(h, direction);
  }

}
