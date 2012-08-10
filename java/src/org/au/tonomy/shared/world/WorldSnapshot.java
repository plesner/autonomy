package org.au.tonomy.shared.world;

import java.util.Collection;
import java.util.Map;

import org.au.tonomy.client.control.Control;
import org.au.tonomy.shared.util.Factory;

/**
 * Encapsulates the state of the world at a whole clock tick.
 */
public class WorldSnapshot {

  private final World world;
  private final Map<Hex, Unit> hexToUnit = Factory.newHashMap();
  private final Map<Unit, Hex> unitToHex = Factory.newHashMap();
  private final int step;

  public WorldSnapshot(World world, int step) {
    this.world = world;
    this.step = step;
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

  public WorldSnapshot advance(Control control) {
    int nextStep = step + 1;
    WorldSnapshot next = new WorldSnapshot(world, nextStep);
    for (Unit unit : getUnits()) {
      UnitValue value = new UnitValue(unit, this);
      control.invoke(nextStep, value);
      value.commit(next);
    }
    return next;
  }

}
