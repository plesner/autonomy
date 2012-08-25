package org.au.tonomy.shared.util;
/**
 * A simple pair type.
 */
public class Pair<S, T> {

  private final S first;
  private final T second;

  private Pair(S first, T second) {
    this.first = first;
    this.second = second;
  }

  public S getFirst() {
    return first;
  }

  public T getSecond() {
    return second;
  }

  public static final <S, T> Pair<S, T> of(S first, T second) {
    return new Pair<S, T>(first, second);
  }

  @Override
  public String toString() {
    return "(" + first + ", " + second + ")";
  }

  @Override
  public int hashCode() {
    return first.hashCode() ^ second.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    } else if (!(obj instanceof Pair<?, ?>)) {
      return false;
    } else {
      Pair<?, ?> that = (Pair<?, ?>) obj;
      return getFirst().equals(that.getFirst())
          && getSecond().equals(that.getSecond());
    }
  }

}
