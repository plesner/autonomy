package org.au.tonomy.shared.ot;

import java.util.Iterator;

import org.au.tonomy.shared.util.Assert;

/**
 * A stream of operations read from a transform.
 */
public class OperationInputStream {

  private final Iterator<Operation> ops;
  private Operation current;

  public OperationInputStream(Transform transform) {
    this.ops = transform.iterator();
    this.advance();
  }

  /**
   * Advances to the next operation. Returns the current one.
   */
  public Operation advance() {
    Operation result = this.current;
    this.current = ops.hasNext() ? ops.next() : null;
    return result;
  }

  /**
   * Returns the current operation.
   */
  public Operation getCurrent() {
    return Assert.notNull(this.current);
  }

  /**
   * Overrides the current operation.
   */
  public void setCurrent(Operation value) {
    this.current = Assert.notNull(value);
  }

  /**
   * Does this stream have a current operation or are we at the end?
   */
  public boolean hasCurrent() {
    return current != null;
  }

  /**
   * Apply all remaining operations to the given output stream.
   */
  public void xformFlush(OperationOutputStream source, OperationOutputStream target) {
    while (hasCurrent())
      advance().xformFlush(source, target);
  }

  public void composeFlush(OperationOutputStream out) {
    while (hasCurrent())
      advance().apply(out);
  }

}