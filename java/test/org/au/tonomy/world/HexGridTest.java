package org.au.tonomy.world;

import junit.framework.TestCase;

import org.au.tonomy.shared.world.HexGrid;
import org.junit.Test;

public class HexGridTest extends TestCase {

  @Test
  public void testClip() {
    assertEquals(0, HexGrid.clip(0, 5));
    assertEquals(1, HexGrid.clip(1, 5));
    assertEquals(2, HexGrid.clip(2, 5));
    assertEquals(3, HexGrid.clip(3, 5));
    assertEquals(4, HexGrid.clip(4, 5));
    assertEquals(0, HexGrid.clip(5, 5));
    assertEquals(1, HexGrid.clip(6, 5));
    assertEquals(2, HexGrid.clip(7, 5));
    assertEquals(3, HexGrid.clip(8, 5));
    assertEquals(4, HexGrid.clip(9, 5));
    assertEquals(4, HexGrid.clip(-1, 5));
    assertEquals(3, HexGrid.clip(-2, 5));
    assertEquals(2, HexGrid.clip(-3, 5));
    assertEquals(1, HexGrid.clip(-4, 5));
    assertEquals(0, HexGrid.clip(-5, 5));
  }

}
