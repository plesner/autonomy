package org.au.tonomy.shared.plankton;

import junit.framework.TestCase;

import org.au.tonomy.shared.plankton.IPlanktonFactory.IPlanktonArray;
import org.au.tonomy.shared.plankton.IPlanktonFactory.IPlanktonMap;
import org.au.tonomy.shared.util.IFunction;
import org.au.tonomy.shared.util.Promise;
import org.junit.Test;

import test.org.au.tonomy.shared.plankton.MathService;
import test.org.au.tonomy.shared.plankton.MathService.AddParameters;
import test.org.au.tonomy.shared.plankton.MathService.IServer;
import test.org.au.tonomy.shared.plankton.MathService.MultParameters;
import test.org.au.tonomy.shared.plankton.MathService.NegParameters;
import test.org.au.tonomy.shared.plankton.MathService.NewPointParameters;
import test.org.au.tonomy.shared.plankton.Point;
import test.org.au.tonomy.shared.plankton.Rect;
import test.org.au.tonomy.shared.plankton.Strip;

public class GenTest extends TestCase {

  private static IPlanktonMap newMap() {
    return Plankton.getDefaultFactory().newMap();
  }

  private static IPlanktonArray newArray() {
    return Plankton.getDefaultFactory().newArray();
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

  @Test
  public void testCompound() {
    Strip s0 = Strip.parse(newMap()
        .set("points", newArray()
            .push(newMap()
                .set("x", 12)
                .set("y", 54))
            .push(newMap()
                .set("x", 3))));
    assertEquals(2, s0.getPoints().size());
    assertEquals(12, s0.getPoints().get(0).getX());
    assertEquals(54, s0.getPoints().get(0).getY());
    assertEquals(3, s0.getPoints().get(1).getX());
    assertEquals(0, s0.getPoints().get(1).getY());
    Strip s1 = Strip.parse(newMap());
    assertEquals(0, s1.getPoints().size());
    Strip s2 = Strip
        .newBuilder()
        .addToPoints(Point
            .newBuilder()
            .setX(1)
            .build())
        .build();
    assertEquals(1, s2.getPoints().size());
    assertEquals(1, s2.getPoints().get(0).getX());
  }

  /**
   * Serializes and deserializes an object as plankton.
   */
  private static Object transcode(Object arg) {
    StringBinaryOutputStream out = new StringBinaryOutputStream();
    Plankton.encode(arg, out);
    String str = out.flush();
    return Plankton.decode(new StringBinaryInputStream(str));
  }

  /**
   * Returns a function that serializes and deserializes the input,
   * passes it to the given server, and then serializes and deserializes
   * the result before it's returned.
   */
  private static IFunction<RemoteMessage, Promise<?>> getSender(final IServer server) {
    return new IFunction<RemoteMessage, Promise<?>>() {
      @Override
      public Promise<?> call(RemoteMessage arg) {
        RemoteMessage message = RemoteMessage.parse(transcode(arg));
        Promise<?> rawResult = MathService.dispatch(message, server);
        return rawResult.then(new IFunction<Object, Object>() {
          @Override
          public Object call(Object arg) {
            return transcode(arg);
          }
        });
      }
    };
  }

  @Test
  public void testService() {
    MathService.IClient client = MathService.newEncoder(getSender(new MathServiceImpl()));
    assertEquals(848, (int) client.mult(
        MultParameters
            .newBuilder()
            .setA(16)
            .setB(53)
            .build())
        .getValue());
    assertEquals(69, (int) client.add(
        AddParameters
            .newBuilder()
            .setA(16)
            .setB(53)
            .build())
        .getValue());
    assertEquals(-6, (int) client.neg(
        NegParameters
            .newBuilder()
            .setValue(6)
            .build())
        .getValue());
    Point p0 = client.newPoint(
        NewPointParameters
            .newBuilder()
            .setX(43)
            .setY(48)
            .build())
        .getValue();
    assertEquals(43, p0.getX());
    assertEquals(48, p0.getY());
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

    @Override
    public Promise<? extends Point> newPoint(NewPointParameters params) {
      return Promise.of(Point
          .newBuilder()
          .setX(params.getX())
          .setY(params.getY())
          .build());
    }

  }

}
