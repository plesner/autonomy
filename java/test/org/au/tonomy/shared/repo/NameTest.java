package org.au.tonomy.shared.repo;

import junit.framework.TestCase;

import org.junit.Test;

public class NameTest extends TestCase {

  @Test
  public void testNames() {
    assertEquals(Name.of("foo"), Name.of("foo"));
    assertEquals(Name.of("foo", "bar"), Name.of("foo", "bar"));
    assertFalse(Name.of("foo", "bar").equals(Name.of("foo")));
  }

}
