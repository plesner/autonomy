package org.au.tonomy.shared.plankton;

import junit.framework.TestCase;

import org.au.tonomy.shared.plankton.IPlanktonFactory.IPlanktonMap;
import org.junit.Test;

import test.org.au.tonomy.shared.plankton.PPoint;
import test.org.au.tonomy.shared.plankton.PRect;

public class GenTest extends TestCase {

  private static IPlanktonMap newMap() {
    return Plankton.getDefaultFactory().newMap();
  }

  @Test
  public void testSimple() {
    PPoint p0 = PPoint
        .newBuilder()
        .setX(2341)
        .setY(5344)
        .build();
    assertEquals(2341, p0.getX());
    assertEquals(5344, p0.getY());
  }

  @Test
  public void testMissingFields() throws ParseError {
    PPoint p0 = PPoint.parse(newMap()
        .set("x", 32)
        .set("y", 43));
    assertEquals(32, p0.getX());
    assertEquals(43, p0.getY());
    PPoint p1 = PPoint.parse(newMap()
        .set("x", 54));
    assertEquals(54, p1.getX());
    assertEquals(0, p1.getY());
    PPoint p2 = PPoint.parse(newMap());
    assertEquals(0, p2.getX());
    assertEquals(0, p2.getY());
  }

  @Test
  public void testIllegalFields() {
    try {
      PPoint.parse(newMap().set("x", "abc"));
      fail();
    } catch (ParseError pe) {
      assertEquals("x", pe.getPath());
    }
    try {
      PRect.parse(newMap()
          .set("top_left", newMap()
              .set("x", 0)
              .set("y", "foo")));
      fail();
    } catch (ParseError pe) {
      assertEquals("top_left.y", pe.getPath());
    }
    try {
      PRect.parse(newMap()
          .set("top_left", null));
      fail();
    } catch (ParseError pe) {
      assertEquals("top_left", pe.getPath());
    }
  }

}
