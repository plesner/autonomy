package org.au.tonomy.shared.world;

import static org.au.tonomy.shared.util.ExtraMath.TAU;
import static org.au.tonomy.testing.TestUtils.assertClose;
import junit.framework.TestCase;

import org.junit.Test;

public class HexTest extends TestCase {

  @Test
  public void testCenter() {
    Hex origin = new Hex(0, 0);
    assertClose(0, origin.getCenterX());
    assertClose(0, origin.getCenterY());
    Hex right = new Hex(1, 0);
    assertClose(2 * Math.sin(TAU / 6), right.getCenterX());
    assertClose(0, right.getCenterY());
    Hex up = new Hex(0, 1);
    assertClose(Math.sin(TAU / 6), up.getCenterX());
    assertClose(1.5, up.getCenterY());
  }

}
