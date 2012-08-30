package org.au.tonomy.shared.plankton;

import java.util.Arrays;
import java.util.Collections;

import junit.framework.TestCase;

import org.junit.Test;

public class PlanktonTest extends TestCase {

  private void checkCoding(Object value) throws DecodingError {
    StringBinaryOutputStream out = new StringBinaryOutputStream();
    Plankton.encode(value, out);
    String data = out.flush();
    StringBinaryInputStream in = new StringBinaryInputStream(data);
    Object decoded = Plankton.decode(in);
    assertEquals(value, decoded);
  }

  @Test
  public void testCoding() throws DecodingError {
    checkCoding(true);
    checkCoding(false);
    checkCoding(null);
    checkCoding(-1800);
    checkCoding(0);
    checkCoding(6583);
    checkCoding("foobar");
    checkCoding("");
    checkCoding("foo\0bar");
    checkCoding(Arrays.asList(1, 2, 3));
    checkCoding(Arrays.asList());
    checkCoding(Arrays.asList(1, "2", Arrays.asList(3, 4)));
    checkCoding(Collections.emptyMap());
    checkCoding(Collections.singletonMap(1, "blah"));
  }

  @Test
  public void testByteArrayStreams() throws DecodingError {
    StringBinaryOutputStream out = new StringBinaryOutputStream();
    out.addByte(1);
    out.addByte(8);
    out.addByte(127);
    out.addBlob(new byte[] {1, 8, 127});
    out.addInt32(6550892);
    out.addInt32(-6550892);
    out.addInt32(0);
    StringBinaryInputStream in = new StringBinaryInputStream(out.flush());
    assertEquals(1, in.nextByte());
    assertEquals(8, in.nextByte());
    assertEquals(127, in.nextByte());
    byte[] blob = in.nextBlob();
    assertEquals(3, blob.length);
    assertEquals(1, blob[0]);
    assertEquals(8, blob[1]);
    assertEquals(127, blob[2]);
    assertEquals(6550892, in.nextInt32());
    assertEquals(-6550892, in.nextInt32());
    assertEquals(0, in.nextInt32());
  }

}
