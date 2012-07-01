package org.au.tonomy.shared.world;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.au.tonomy.shared.world.Hex.Side;

/**
 * Encapsulates the state of the world at a whole clock tick.
 */
public class WorldSnapshot {

  private final World world;
  private final Map<Hex, Unit> hexToUnit = new HashMap<Hex, Unit>();
  private final Map<Unit, Hex> unitToHex = new HashMap<Unit, Hex>();

  public WorldSnapshot(World world) {
    this.world = world;
  }

  public World getWorld() {
    return this.world;
  }

  public void spawnUnit(int g, int h) {
    Hex hex = world.getGrid().getHex(g, h);
    Unit unit = world.newUnit();
    setUnit(hex, unit);
  }

  public void setUnit(Hex hex, Unit unit) {
    hexToUnit.put(hex, unit);
    unitToHex.put(unit, hex);
  }

  public Collection<Unit> getUnits() {
    return hexToUnit.values();
  }

  public Hex getLocation(Unit unit) {
    return unitToHex.get(unit);
  }

  public WorldSnapshot advance() {
    WorldSnapshot next = new WorldSnapshot(world);
    Side[] sides = Side.values();
    Side side = sides[random.nextInt(sides.length)];
    for (Unit unit : getUnits()) {
      Hex hex = getLocation(unit);
      next.setUnit(world.getGrid().getNeighbour(hex, side), unit);
    }
    return next;
  }

  private static final Random random = new Random(32);

}
