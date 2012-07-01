package org.au.tonomy.shared.world;
public class Unit {

  private final int id;

  public Unit(int id) {
    this.id = id;
  }

  @Override
  public int hashCode() {
    return this.id;
  }

  @Override
  public boolean equals(Object obj) {
    return (this == obj);
  }

}
