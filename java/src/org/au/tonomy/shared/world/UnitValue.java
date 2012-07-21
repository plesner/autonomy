package org.au.tonomy.shared.world;

import org.au.tonomy.shared.runtime.AbstractValue;
import org.au.tonomy.shared.runtime.IValue;
import org.au.tonomy.shared.runtime.MethodRegister;

public class UnitValue extends AbstractValue {

  private static final MethodRegister<UnitValue> METHODS = new MethodRegister<UnitValue>() {{
    addMethod(".move", new IMethod<UnitValue>() {
      @Override
      public IValue invoke(UnitValue self, IValue[] args) {
        int index = args[0].getIntValue();
        self.move = Hex.Side.values()[index];
        return self;
      }
    });
  }};

  private final Unit unit;
  private final WorldSnapshot current;
  private Hex.Side move = null;

  public UnitValue(Unit unit, WorldSnapshot current) {
    this.unit = unit;
    this.current = current;
  }

  @Override
  public IValue invoke(String name, IValue[] args) {
    return METHODS.invoke(name, this, args);
  }

  /**
   * Records in the given next snapshot information about this unit,
   * given the pending changes recorded.
   */
  public void commit(WorldSnapshot next) {
    Hex currentLocation = current.getLocation(unit);
    if (move == null) {
      next.setUnit(currentLocation, unit);
    } else {
      HexGrid grid = current.getWorld().getGrid();
      next.setUnit(grid.getNeighbour(currentLocation, move), unit);
    }
  }

}
