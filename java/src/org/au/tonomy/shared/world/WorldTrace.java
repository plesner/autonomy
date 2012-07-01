package org.au.tonomy.shared.world;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.au.tonomy.shared.util.Assert;

/**
 * A trace of the changes taking place in a world object.
 */
public class WorldTrace {

  public interface IUnitState {

    /**
     * Hex this unit is moving from.
     */
    public Hex getFrom();

    /**
     * Hex this unit is moving to.
     */
    public Hex getTo();

    /**
     * Returns how far the unit has come in its movement.
     */
    public double getProgress();

  }

  private final World world;
  private final List<WorldSnapshot> timeline = new ArrayList<WorldSnapshot>();

  public WorldTrace(World world, WorldSnapshot initial) {
    this.world = world;
    timeline.add(initial);
  }

  public World getWorld() {
    return this.world;
  }

  /**
   * Returns an iterable over the complete unit state at the given
   * time. Each returned value becomes invalid when the next one is
   * returned so don't store and use them.
   */
  public Iterable<IUnitState> getUnits(double time) {
    Assert.that(time >= 0);
    int beforeTurn = (int) Math.floor(time);
    int afterTurn = (int) Math.ceil(time);
    final WorldSnapshot before = getSnapshot(beforeTurn);
    final WorldSnapshot after = getSnapshot(afterTurn);
    final double progress = time - beforeTurn;
    return new Iterable<IUnitState>() {
      @Override
      public Iterator<IUnitState> iterator() {
        return new UnitIterator(before, after, progress);
      }
    };
  }

  private WorldSnapshot getSnapshot(int index) {
    if (index < timeline.size()) {
      return timeline.get(index);
    } else {
      WorldSnapshot result = calcSnapshot(index);
      Assert.that(timeline.size() == index);
      timeline.add(result);
      return result;
    }
  }

  private WorldSnapshot calcSnapshot(int index) {
    WorldSnapshot prev = getSnapshot(index - 1);
    return prev.advance();
  }

  /**
   * An iterator over the units between two snapshots that cheats by
   * returning the same unit state every time, which happens to be
   * this same object.
   */
  private static class UnitIterator implements Iterator<IUnitState>,
      IUnitState {

    private final WorldSnapshot before;
    private final WorldSnapshot after;
    private final Iterator<Unit> beforeUnits;
    private final double progress;
    private Unit current;

    public UnitIterator(WorldSnapshot before, WorldSnapshot after,
        double progress) {
      this.before = before;
      this.after = after;
      this.beforeUnits = before.getUnits().iterator();
      this.progress = progress;
    }

    @Override
    public boolean hasNext() {
      return beforeUnits.hasNext();
    }

    @Override
    public IUnitState next() {
      current = beforeUnits.next();
      return this;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }

    @Override
    public Hex getFrom() {
      return before.getLocation(current);
    }

    @Override
    public Hex getTo() {
      return after.getLocation(current);
    }

    @Override
    public double getProgress() {
      return progress;
    }

  }

}
