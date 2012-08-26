package org.au.tonomy.agent;

import java.util.Arrays;
import java.util.TreeMap;

import junit.framework.TestCase;

import org.au.tonomy.shared.ot.IJsonable;
import org.junit.Test;
@SuppressWarnings("serial")
public class JsonTest extends TestCase {

  private static void check(String expected, Object value) {
    assertEquals(expected, Json.stringify(value));
  }

  @Test
  public void testSimpleJson() {
    check("[1,2,3]", Arrays.asList(1, 2, 3));
    check("\"foo\"", "foo");
    check("true", true);
    check("false", false);
    check("null", null);
    check("\"f\\noo\"", "f\noo");
    check("\"\\r\\n\\\"\\\\\"", "\r\n\"\\");
    check("\"\\u0000\"", "\0");
    check("\"\\u000b\"", "\u000b");
    check("{\"a\":4}", new JsonMap() {{ put("a", 4); }});
    check("{\"a\":4,\"b\":\"foo\"}", new JsonMap() {{
      put("a", 4);
      put("b", "foo");
    }});
    check("8", new IJsonable() {
      @Override
      public Object toJson() {
        return 8;
      }
    });
  }

  private static class JsonMap extends TreeMap<String, Object> { }

}
