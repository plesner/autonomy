package org.au.tonomy.shared.repo;

import java.util.Arrays;
import java.util.List;

import org.au.tonomy.shared.util.Assert;

/**
 * The name of a source package.
 */
public class Name {

  private final List<String> parts;

  /**
   * Creates a new name consisting of the given parts.
   */
  public Name(List<String> parts) {
    Assert.that(parts.size() > 0);
    this.parts = parts;
  }

  /**
   * Creates a new name consisting of the given parts.
   */
  public static Name of(String... parts) {
    return new Name(Arrays.asList(parts));
  }

  @Override
  public int hashCode() {
    return parts.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    } else if (!(obj instanceof Name)) {
      return false;
    } else {
      Name that = (Name) obj;
      return that.parts.equals(this.parts);
    }
  }

}
