package org.au.tonomy.shared.source;

import java.util.Map;
import java.util.TreeMap;

import org.au.tonomy.shared.ot.Transform;
import org.au.tonomy.shared.util.Factory;


/**
 * A utility that can map between a (row, col) based coordinate
 * system and a simple flat (offset) system.
 */
public class SourceCoordinateMapper {

  private TreeMap<Integer, Integer> rowToOffset;
  private TreeMap<Integer, Integer> negOffsetToRow;
  private String text;

  public SourceCoordinateMapper() {
    resetSource("");
  }

  /**
   * Resets the source code to map.
   */
  public void resetSource(String text) {
    this.text = text;
    recomputeMaps();
  }

  /**
   * Apply the given transformation to this mapper's source and
   * update its internal state to reflect the changes.
   */
  public void apply(Transform transform) {
    resetSource(transform.call(this.text));
  }

  private void recomputeMaps() {
    rowToOffset = Factory.newTreeMap();
    negOffsetToRow = Factory.newTreeMap();
    int rowIndex = 0;
    rowToOffset.put(0, 0);
    negOffsetToRow.put(0, 0);
    for (int i = 0; i < this.text.length(); i++) {
      if (this.text.charAt(i) == '\n') {
        rowIndex++;
        rowToOffset.put(rowIndex, i + 1);
        negOffsetToRow.put(-(i + 1), rowIndex);
      }
    }
  }

  /**
   * Returns the flat offset of the given row/col coordinates.
   */
  public int getOffset(int row, int col) {
    return rowToOffset.get(row) + col;
  }

  /**
   * Returns the row/col of the given offset.
   */
  public int[] getRowCol(int offset) {
    Map.Entry<Integer, Integer> entry = negOffsetToRow.tailMap(-offset).entrySet().iterator().next();
    return new int[] { entry.getValue(), offset + entry.getKey() };
  }

}
