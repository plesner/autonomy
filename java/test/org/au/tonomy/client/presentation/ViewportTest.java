package org.au.tonomy.client.presentation;

import static org.au.tonomy.testing.TestUtils.D;
import static org.au.tonomy.testing.TestUtils.assertClose;
import junit.framework.TestCase;

import org.au.tonomy.shared.world.HexGrid;
import org.au.tonomy.testing.JavaMatrix;
import org.au.tonomy.testing.JavaVector;
import org.junit.Test;

public class ViewportTest extends TestCase {

  private static class TestViewport extends Viewport<JavaVector, JavaMatrix> {

    public TestViewport(int width, int height) {
      super(new HexGrid(width, height));
    }

  }

  @Test
  public void testMaxBounds() {
    assertClose(D, new TestViewport(2, 2).getMaxWidth());
    assertClose(2 * D, new TestViewport(3, 2).getMaxWidth());
    assertClose(9 * D, new TestViewport(10, 2).getMaxWidth());
    assertClose(9 * D, new TestViewport(10, 10).getMaxWidth());
    assertClose(1, new TestViewport(2, 2).getMaxHeight());
    assertClose(2.5, new TestViewport(2, 3).getMaxHeight());
    assertClose(13, new TestViewport(2, 10).getMaxHeight());
    assertClose(13, new TestViewport(10, 10).getMaxHeight());
  }

}
