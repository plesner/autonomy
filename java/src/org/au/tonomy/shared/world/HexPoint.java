package org.au.tonomy.shared.world;
/**
 * A simple mutable integer (g, h) hex point.
 */
public class HexPoint {

  private final int g;
  private final int h;

  public HexPoint(int g, int h) {
    this.g = g;
    this.h = h;
  }

  public int getG() {
    return this.g;
  }

  public int getH() {
    return this.h;
  }

  @Override
  public String toString() {
    return "<" + g + ", " + h + ">";
  }

  @Override
  public int hashCode() {
    return g ^ h;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    } else if (!(obj instanceof HexPoint)) {
      return false;
    } else {
      HexPoint that = (HexPoint) obj;
      return (g == that.g) && (h == that.h);
    }
  }

}
