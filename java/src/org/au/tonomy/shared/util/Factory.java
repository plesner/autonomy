package org.au.tonomy.shared.util;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A collection of factory methods for built-in Java types, following
 * the pattern from effective java.
 */
public class Factory {

  /**
   * Creates a new array list.
   */
  public static <T> ArrayList<T> newArrayList() {
    return new ArrayList<T>();
  }

  /**
   * Creates a new hash map.
   */
  public static <K, V> HashMap<K, V> newHashMap() {
    return new HashMap<K, V>();
  }

}
