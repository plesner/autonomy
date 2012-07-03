package org.au.tonomy.shared.util;
/**
 * Utilities for working with exceptions.
 */
public class Exceptions {

  /**
   * Simply throws the argument, if it is a runtime exception, or
   * wraps it in a runtime exception and throws that. This function
   * doesn't return but its return type can be used in a throw at the
   * call site to make it clear to the compiler that execution doesn't
   * continue after this has returned.
   */
  public static RuntimeException propagate(Throwable error) {
    if (error instanceof RuntimeException) {
      throw (RuntimeException) error;
    } else {
      throw new RuntimeException(error);
    }
  }

}
