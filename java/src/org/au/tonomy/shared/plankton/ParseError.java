package org.au.tonomy.shared.plankton;

import java.util.List;

import org.au.tonomy.shared.util.Factory;
/**
 * Exception signalling that parsing data as a plankton object failed.
 */
@SuppressWarnings("serial")
public class ParseError extends RuntimeException {

  private final List<String> path = Factory.newArrayList();

  @Override
  public String toString() {
    return "ParseError: " + getPath();
  }

  /**
   * Returns the path of fields to the point where this parse error
   * occurred.
   */
  public String getPath() {
    StringBuilder buf = new StringBuilder();
    int end = path.size() - 1;
    for (int i = end; i >= 0; i--) {
      if (i < end)
        buf.append(".");
      buf.append(path.get(i));
    }
    return buf.toString();
  }

  /**
   * Adds a field to the sequence of fields that identifies where the
   * parse error occurred.
   */
  public ParseError addPathSegment(String name) {
    this.path.add(name);
    return this;
  }

}
