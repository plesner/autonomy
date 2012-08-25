package org.au.tonomy.shared.source;

import junit.framework.TestCase;

import org.au.tonomy.shared.util.Pair;
import org.junit.Test;

public class SourceCoordinateMapperTest extends TestCase {

  private void checkMap(SourceCoordinateMapper map, int offset, int row, int col) {
    assertEquals(offset, map.getOffset(row, col));
    int[] rowCol = map.getRowCol(offset);
    assertEquals(Pair.of(row, col), Pair.of(rowCol[0], rowCol[1]));
  }

  @Test
  public void testSimpleMapping() {
    SourceCoordinateMapper map = new SourceCoordinateMapper();
    map.resetSource("foo\nbaaaar\nbaz");
    checkMap(map, 0, 0, 0);
    checkMap(map, 1, 0, 1);
    checkMap(map, 2, 0, 2);
    checkMap(map, 3, 0, 3);
    checkMap(map, 4, 1, 0);
    checkMap(map, 5, 1, 1);
    checkMap(map, 6, 1, 2);
    checkMap(map, 7, 1, 3);
    checkMap(map, 8, 1, 4);
    checkMap(map, 9, 1, 5);
    checkMap(map, 10, 1, 6);
    checkMap(map, 11, 2, 0);
    checkMap(map, 12, 2, 1);
    checkMap(map, 13, 2, 2);
  }

  @Test
  public void testEmpty() {
    SourceCoordinateMapper map = new SourceCoordinateMapper();
    checkMap(map, 0, 0, 0);
  }

}
