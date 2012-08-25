package org.au.tonomy.agent;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.au.tonomy.shared.util.Factory;

/**
 * Utilities for converting java objects to JSON.
 */
public class Json {

  /**
   * A utility for consing up a json map.
   */
  public static class JsonMap implements IJsonable {

    private final HashMap<Object, Object> elms = Factory.newHashMap();

    /**
     * Add a mappint to this json map.
     */
    protected void put(Object key, Object value) {
      elms.put(key, value);
    }

    @Override
    public Object toJson() {
      return elms;
    }

  }

  /**
   * Converts the given json-like object to a json string.
   */
  public static String stringify(Object value) {
    StringBuilder buf = new StringBuilder();
    writeJson(value, buf);
    return buf.toString();
  }

  private static void writeEscapedString(String str, StringBuilder buf) {
    for (int i = 0; i < str.length(); i++) {
      char c = str.charAt(i);
      switch (c) {
      case '"':
        buf.append("\\\"");
        break;
      case '\\':
        buf.append("\\\\");
        break;
      case '\b':
        buf.append("\\b");
        break;
      case '\f':
        buf.append("\\f");
        break;
      case '\n':
        buf.append("\\n");
        break;
      case '\r':
        buf.append("\\r");
        break;
      case '\t':
        buf.append("\\t");
        break;
      default:
        if (c < ' ') {
          buf.append("\\u00");
          for (int j = 1; j >= 0; j--) {
            int nibble = c & (0xF << (4 * j));
            buf.append("0123456789ABCDEF".charAt(nibble));
          }
        } else {
          buf.append(c);
        }
        break;
      }
    }
  }

  private static void writeJson(Object value, StringBuilder buf) {
    if (value instanceof IJsonable) {
      writeJson(((IJsonable) value).toJson(), buf);
    } else if (value instanceof String) {
      buf.append('"');
      writeEscapedString((String) value, buf);
      buf.append('"');
    } else if (value instanceof Integer) {
      buf.append(((Integer) value).intValue());
    } else if (value instanceof Map<?, ?>) {
      buf.append('{');
      boolean first = true;
      for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
        if (first) first = false;
        else buf.append(',');
        writeJson(entry.getKey(), buf);
        buf.append(':');
        writeJson(entry.getValue(), buf);
      }
      buf.append('}');
    } else if (value instanceof Collection<?>) {
      buf.append('[');
      boolean first = true;
      for (Object elm : (Collection<?>) value) {
        if (first) first = false;
        else buf.append(',');
        writeJson(elm, buf);
      }
      buf.append(']');
    } else if (value instanceof Number) {
      buf.append(((Number) value).doubleValue());
    } else if (value instanceof Boolean) {
      buf.append(((Boolean) value).booleanValue() ? "true" : "false");
    } else if (value == null) {
      buf.append("null");
    }
  }

}
