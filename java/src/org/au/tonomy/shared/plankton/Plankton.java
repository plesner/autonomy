package org.au.tonomy.shared.plankton;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.au.tonomy.shared.plankton.IPlanktonFactory.IPlanktonArray;
import org.au.tonomy.shared.plankton.IPlanktonFactory.IPlanktonMap;
import org.au.tonomy.shared.util.Assert;
import org.au.tonomy.shared.util.Exceptions;
import org.au.tonomy.shared.util.Factory;
import org.au.tonomy.shared.util.Internal;


/**
 * Container class for methods for encoding and decoding plankton.
 */
public class Plankton {

  private static final byte tBool = 0;
  private static final byte tString = 1;
  private static final byte tInteger = 2;
  private static final byte tMap = 3;
  private static final byte tList = 4;
  private static final byte tNull = 5;

  /**
   * Encodes an object as binary plankton, outputting on the given
   * stream.
   */
  public static void encode(Object value, IBinaryOutputStream out) {
    if (value instanceof String) {
      out.addByte(tString);
      encodeString((String) value, out);
    } else if (value instanceof Integer) {
      out.addByte(tInteger).addInt32(((Integer) value).intValue());
    } else if (value instanceof Boolean) {
      out.addByte(tBool).addByte(value == Boolean.TRUE ? 1 : 0);
    } else if (value instanceof Map<?, ?>) {
      Map<?, ?> map = (Map<?, ?>) value;
      out.addByte(tMap).addInt32(map.size());
      for (Map.Entry<?, ?> entry : map.entrySet()) {
        encode(entry.getKey(), out);
        encode(entry.getValue(), out);
      }
    } else if (value instanceof Collection<?>) {
      Collection<?> coll = (Collection<?>) value;
      out.addByte(tList).addInt32(coll.size());
      for (Object elm : coll)
        encode(elm, out);
    } else if (value == null) {
      out.addByte(tNull);
    } else if (value instanceof IPlanktonDatable) {
      encode(((IPlanktonDatable) value).toPlanktonData(FACTORY), out);
    } else if (value instanceof IPlanktonable<?>) {
      encode(((IPlanktonable<?>) value).toPlankton(), out);
    } else {
      Assert.that(false);
    }
  }

  /**
   * Reads a stream of binary plankton, returning the object represented.
   * Throws a {@link DecodingError} if the input is somehow invalid.
   */
  public static Object decode(IBinaryInputStream in) throws DecodingError {
    switch (in.nextByte()) {
    case tBool:
      return (in.nextByte() == 1) ? true : false;
    case tString:
      return decodeString(in);
    case tInteger:
      return in.nextInt32();
    case tMap: {
      int entries = in.nextInt32();
      Map<Object, Object> result = Factory.newHashMap();
      for (int i = 0; i < entries; i++) {
        Object key = decode(in);
        Object value = decode(in);
        result.put(key, value);
      }
      return result;
    }
    case tList: {
      int length = in.nextInt32();
      List<Object> result = Factory.newArrayList();
      for (int i = 0; i < length; i++)
        result.add(decode(in));
      return result;
    }
    case tNull:
      return null;
    default:
      throw new DecodingError("Unexpected tag value");
    }
  }

  /**
   * Encodes a string as utf8 on the given stream.
   */
  private static void encodeString(String value, IBinaryOutputStream out) {
    byte[] bytes;
    try {
      bytes = value.getBytes("UTF-8");
    } catch (UnsupportedEncodingException uee) {
      throw Exceptions.propagate(uee);
    }
    out.addBlob(bytes);
  }

  /**
   * Reads a utf8 string from the given stream.
   */
  private static String decodeString(IBinaryInputStream in) throws DecodingError {
    byte[] bytes = in.nextBlob();
    try {
      return new String(bytes, "UTF-8");
    } catch (UnsupportedEncodingException uee) {
      throw Exceptions.propagate(uee);
    }
  }

  @Internal
  public static IPlanktonFactory getDefaultFactory() {
    return FACTORY;
  }

  private static final IPlanktonFactory FACTORY = new IPlanktonFactory() {
    @Override
    public IPlanktonMap newMap() {
      return new JsonMap();
    }
    @Override
    public IPlanktonArray newArray() {
      return new JsonArray();
    }
  };

  /**
   * A utility for consing up a json map.
   */
  @SuppressWarnings("serial")
  private static class JsonMap extends HashMap<String, Object> implements IPlanktonMap {

    @Override
    public JsonMap set(String key, Object value) {
      put(key, value);
      return this;
    }

  }

  /**
   * A utility for consing up a json array.
   */
  @SuppressWarnings("serial")
  private static class JsonArray extends ArrayList<Object> implements IPlanktonArray {

    @Override
    public IPlanktonArray push(Object value) {
      this.add(value);
      return this;
    }

  }

}
