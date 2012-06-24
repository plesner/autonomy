package org.au.tonomy.shared.world;

import static org.au.tonomy.shared.world.HexTest.EPSILON;

import java.util.Random;

import junit.framework.TestCase;

import org.au.tonomy.shared.util.ExtraMath;
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

  @Test
  public void testLocateCenters() {
    HexGrid grid = new HexGrid(32, 32);
    HexPoint point = new HexPoint();
    for (int g = 0; g < 4; g++) {
      for (int h = 0; h < 4; h++) {
        Hex hex = new Hex(g, h);
        grid.locate(hex.getCenterX(), hex.getCenterY(), point);
        assertEquals(g, point.getG());
        assertEquals(h, point.getH());
      }
    }
  }

  @Test
  public void testLocateRandom() {
    Random random = new Random(23142);
    HexGrid grid = new HexGrid(10, 10);
    HexPoint point = new HexPoint();
    for (int g = 0; g < 10; g++) {
      for (int h = 0; h < 10; h++) {
        Hex hex = new Hex(g, h);
        double centerX = hex.getCenterX();
        double centerY = hex.getCenterY();
        for (int t = 0; t < 10; t++) {
          double angle = random.nextDouble() * ExtraMath.TAU;
          double length = random.nextDouble() * Hex.INNER_RADIUS;
          double x = centerX + (Math.cos(angle) * length);
          double y = centerY + (Math.sin(angle) * length);
          grid.locate(x, y, point);
          assertEquals(g, point.getG());
          assertEquals(h, point.getH());
        }
      }
    }
  }

  @Test
  public void testUnitDiagonal() {
    // Around the origin
    assertEquals(0, HexGrid.getUnitDiagonal(0, 0));
    assertEquals(0, HexGrid.getUnitDiagonal(0, -1));
    assertEquals(0, HexGrid.getUnitDiagonal(-1, 0));
    assertEquals(0, HexGrid.getUnitDiagonal(-1, -1));
    assertEquals(0, HexGrid.getUnitDiagonal(1, -1));
    // Moving up the x-axis
    assertEquals(0, HexGrid.getUnitDiagonal(2, -2));
    assertEquals(0, HexGrid.getUnitDiagonal(2, -3));
    assertEquals(0, HexGrid.getUnitDiagonal(1, -3));
    assertEquals(0, HexGrid.getUnitDiagonal(3, -3));
    assertEquals(0, HexGrid.getUnitDiagonal(8, -8));
    // Moving up the y-axis
    assertEquals(1, HexGrid.getUnitDiagonal(1, 0));
    assertEquals(1, HexGrid.getUnitDiagonal(1, 1));
    assertEquals(1, HexGrid.getUnitDiagonal(2, 1));
    assertEquals(1, HexGrid.getUnitDiagonal(2, 0));
    assertEquals(1, HexGrid.getUnitDiagonal(0, 3));
    // Random points elsewhere
    assertEquals(3, HexGrid.getUnitDiagonal(6, 1));
    assertEquals(3, HexGrid.getUnitDiagonal(7, 1));
    assertEquals(3, HexGrid.getUnitDiagonal(6, 2));
    assertEquals(3, HexGrid.getUnitDiagonal(7, 2));
  }

  private static double[] getCenterUnitH(int g, int h) {
    Hex hex = new Hex(g, h);
    double x = HexGrid.getUnitHX(hex.getCenterX(), hex.getCenterY());
    double y = HexGrid.getUnitHY(hex.getCenterX(), hex.getCenterY());
    return new double[] { x, y };
  }

  private static double[] getCenterUnitG(int g, int h) {
    Hex hex = new Hex(g, h);
    double x = HexGrid.getUnitGX(hex.getCenterX(), hex.getCenterY());
    double y = HexGrid.getUnitGY(hex.getCenterX(), hex.getCenterY());
    return new double[] { x, y };
  }

  private static void assertClose(double x, double y, double[] found) {
    HexTest.assertClose(x, found[0]);
    HexTest.assertClose(y, found[1]);
  }

  @Test
  public void testUnitCoordinates() {
    // G coordinate mapping
    assertClose(0, 0, getCenterUnitG(0, 0));
    assertClose(1, -1, getCenterUnitG(0, 1));
    assertClose(2, -2, getCenterUnitG(0, 2));
    assertClose(3, -3, getCenterUnitG(0, 3));
    assertClose(2, 1, getCenterUnitG(1, 0));
    assertClose(4, 2, getCenterUnitG(2, 0));
    assertClose(6, 3, getCenterUnitG(3, 0));
    assertClose(9, 0, getCenterUnitG(3, 3));
    // H coordinate mapping.
    assertClose(0, 0, getCenterUnitH(0, 0));
    assertClose(1, -1, getCenterUnitH(1, 0));
    assertClose(2, -2, getCenterUnitH(2, 0));
    assertClose(3, -3, getCenterUnitH(3, 0));
    assertClose(2, 1, getCenterUnitH(0, 1));
    assertClose(4, 2, getCenterUnitH(0, 2));
    assertClose(6, 3, getCenterUnitH(0, 3));
    assertClose(9, 0, getCenterUnitH(3, 3));
  }

  public void testStrips() {
    // Lower
    assertEquals(0, HexGrid.getLowerRectStrip(0.75));
    assertEquals(0, HexGrid.getLowerRectStrip(1.0 - EPSILON));
    assertEquals(1, HexGrid.getLowerRectStrip(1.0 + EPSILON));
    assertEquals(1, HexGrid.getLowerRectStrip(1.25));
    assertEquals(1, HexGrid.getLowerRectStrip(1.5));
    assertEquals(1, HexGrid.getLowerRectStrip(1.75));
    assertEquals(1, HexGrid.getLowerRectStrip(2.0));
    assertEquals(1, HexGrid.getLowerRectStrip(2.25));
    assertEquals(1, HexGrid.getLowerRectStrip(2.5 - EPSILON));
    assertEquals(2, HexGrid.getLowerRectStrip(2.5 + EPSILON));
    assertEquals(2, HexGrid.getLowerRectStrip(2.75));
    // Upper
    assertEquals(0, HexGrid.getUpperRectStrip(0.0));
    assertEquals(0, HexGrid.getUpperRectStrip(0.25));
    assertEquals(0, HexGrid.getUpperRectStrip(0.5 - EPSILON));
    assertEquals(1, HexGrid.getUpperRectStrip(0.5 + EPSILON));
    assertEquals(1, HexGrid.getUpperRectStrip(0.75));
    assertEquals(1, HexGrid.getUpperRectStrip(1.0));
    assertEquals(1, HexGrid.getUpperRectStrip(1.25));
    assertEquals(1, HexGrid.getUpperRectStrip(1.5));
    assertEquals(1, HexGrid.getUpperRectStrip(1.75));
    assertEquals(1, HexGrid.getUpperRectStrip(2.0 - EPSILON));
    assertEquals(2, HexGrid.getUpperRectStrip(2.0 + EPSILON));
    assertEquals(2, HexGrid.getUpperRectStrip(2.25));
    // Leftmost
    double h = Hex.INNER_RADIUS / 2;
    assertEquals(0, HexGrid.getLeftmostRectStrip(0.0));
    assertEquals(0, HexGrid.getLeftmostRectStrip(h));
    assertEquals(0, HexGrid.getLeftmostRectStrip(2 * h - EPSILON));
    assertEquals(1, HexGrid.getLeftmostRectStrip(2 * h + EPSILON));
    assertEquals(1, HexGrid.getLeftmostRectStrip(3 * h));
    assertEquals(1, HexGrid.getLeftmostRectStrip(4 * h));
    assertEquals(1, HexGrid.getLeftmostRectStrip(5 * h));
    assertEquals(1, HexGrid.getLeftmostRectStrip(6 * h - EPSILON));
    assertEquals(2, HexGrid.getLeftmostRectStrip(6 * h + EPSILON));
    // Rightmost
    assertEquals(1, HexGrid.getRightmostRectStrip(EPSILON));
    assertEquals(1, HexGrid.getRightmostRectStrip(h));
    assertEquals(1, HexGrid.getRightmostRectStrip(2 * h - EPSILON));
    assertEquals(2, HexGrid.getRightmostRectStrip(2 * h + EPSILON));
    assertEquals(2, HexGrid.getRightmostRectStrip(3 * h));
    assertEquals(2, HexGrid.getRightmostRectStrip(4 * h));
    assertEquals(2, HexGrid.getRightmostRectStrip(5 * h));
    assertEquals(2, HexGrid.getRightmostRectStrip(6 * h - EPSILON));
    assertEquals(3, HexGrid.getRightmostRectStrip(6 * h + EPSILON));
  }

}
