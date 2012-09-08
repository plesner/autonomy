package org.au.tonomy.shared.plankton;

import junit.framework.TestCase;

import org.au.tonomy.shared.plankton.IPlanktonFactory.IPlanktonMap;
import org.au.tonomy.shared.util.IFunction;
import org.au.tonomy.shared.util.Promise;
import org.junit.Test;

import test.org.au.tonomy.shared.plankton.MathService;
import test.org.au.tonomy.shared.plankton.MathService.AddParameters;
import test.org.au.tonomy.shared.plankton.MathService.IServer;
import test.org.au.tonomy.shared.plankton.MathService.MultParameters;
import test.org.au.tonomy.shared.plankton.MathService.NegParameters;
import test.org.au.tonomy.shared.plankton.Point;
import test.org.au.tonomy.shared.plankton.Rect;

public class GenTest extends TestCase {

  private static IPlanktonMap newMap() {
    return Plankton.getDefaultFactory().newMap();
  }

  @Test
  public void testSimple() {
    Point p0 = Point
        .newBuilder()
        .setX(2341)
        .setY(5344)
        .build();
    assertEquals(2341, p0.getX());
    assertEquals(5344, p0.getY());
  }

  @Test
  public void testMissingFields() throws ParseError {
    Point p0 = Point.parse(newMap()
        .set("x", 32)
        .set("y", 43));
    assertEquals(32, p0.getX());
    assertEquals(43, p0.getY());
    assertTrue(p0.hasX());
    assertTrue(p0.hasY());
    Point p1 = Point.parse(newMap()
        .set("x", 54));
    assertTrue(p1.hasX());
    assertFalse(p1.hasY());
    assertEquals(54, p1.getX());
    assertEquals(0, p1.getY());
    Point p2 = Point.parse(newMap());
    assertFalse(p2.hasX());
    assertFalse(p2.hasY());
    assertEquals(0, p2.getX());
    assertEquals(0, p2.getY());
  }

  @Test
  public void testIllegalFields() {
    try {
      Point.parse(newMap().set("x", "abc"));
      fail();
    } catch (ParseError pe) {
      assertEquals("x", pe.getPath());
    }
    try {
      Rect.parse(newMap()
          .set("top_left", newMap()
              .set("x", 0)
              .set("y", "foo")));
      fail();
    } catch (ParseError pe) {
      assertEquals("top_left.y", pe.getPath());
    }
    try {
      Rect.parse(newMap()
          .set("top_left", null));
      fail();
    } catch (ParseError pe) {
      assertEquals("top_left", pe.getPath());
    }
  }

  private static IFunction<IPlanktonDatable, Promise<?>> getSender(final IServer server) {
    return new IFunction<IPlanktonDatable, Promise<?>>() {
      @Override
      public Promise<?> call(IPlanktonDatable arg) {
        StringBinaryOutputStream out = new StringBinaryOutputStream();
        Plankton.encode(arg, out);
        String str = out.flush();
        Object message = Plankton.decode(new StringBinaryInputStream(str));
        return MathService.dispatch(Message.parse(message), server);
      }
    };
  }

  @Test
  public void testService() {
    MathService.IClient client = MathService.newEncoder(getSender(new MathServiceImpl()));
    assertEquals(848, (int) client.mult(MultParameters
        .newBuilder()
        .setA(16)
        .setB(53)
        .build())
        .getValue());
    assertEquals(69, (int) client.add(AddParameters
        .newBuilder()
        .setA(16)
        .setB(53)
        .build())
        .getValue());
    assertEquals(-6, (int) client.neg(NegParameters
        .newBuilder()
        .setValue(6)
        .build())
        .getValue());
  }

  private static class MathServiceImpl implements MathService.IServer {

    @Override
    public Promise<Integer> mult(MultParameters params) {
      return Promise.of(params.getA() * params.getB());
    }

    @Override
    public Promise<Integer> add(AddParameters params) {
      return Promise.of(params.getA() + params.getB());
    }

    @Override
    public Promise<Integer> neg(NegParameters params) {
      return Promise.of(-params.getValue());
    }

  }

}
