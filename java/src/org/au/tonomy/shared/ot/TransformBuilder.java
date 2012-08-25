package org.au.tonomy.shared.ot;

import java.util.List;

import org.au.tonomy.shared.util.Assert;
import org.au.tonomy.shared.util.Factory;

/**
 * A stream of operations used to build a transform.
 */
public class TransformBuilder {

  /**
   * A builder specialized for consing up a particular kind of operation.
   */
  private interface IOperationBuilder {

    /**
     * Skip count characters.
     */
    public IOperationBuilder skip(int count);

    /**
     * Insert the specified text.
     */
    public IOperationBuilder insert(String text);

    /**
     * Delete the specified text.
     */
    public IOperationBuilder delete(String text);

    /**
     * Flush this operation into the operation array.
     */
    public void flush();

  }

  /**
   * A builder with no state that delegates to a specialized builder
   * on each operation.
   */
  private class EmptyBuilder implements IOperationBuilder {

    @Override
    public IOperationBuilder skip(int count) {
      flush();
      return new SkipBuilder().skip(count);
    }

    @Override
    public IOperationBuilder insert(String text) {
      flush();
      return new InsertBuilder().insert(text);
    }

    @Override
    public IOperationBuilder delete(String text) {
      flush();
      return new DeleteBuilder().delete(text);
    }

    public void flush() { }

  }

  /**
   * Builds insert operations.
   */
  private class InsertBuilder extends EmptyBuilder {

    private final StringBuilder buf = new StringBuilder();

    @Override
    public IOperationBuilder insert(String text) {
      buf.append(text);
      return this;
    }

    @Override
    public void flush() {
      ops.add(new Operation.Insert(buf.toString()));
    }

  }

  /**
   * Builds delete operations.
   */
  private class DeleteBuilder extends EmptyBuilder {

    private final StringBuilder buf = new StringBuilder();

    @Override
    public IOperationBuilder delete(String text) {
      buf.append(text);
      return this;
    }

    @Override
    public void flush() {
      ops.add(new Operation.Delete(buf.toString()));
    }

  }

  /**
   * Builds skip operations.
   */
  private class SkipBuilder extends EmptyBuilder {

    private int count = 0;

    @Override
    public IOperationBuilder skip(int count) {
      this.count += count;
      return this;
    }

    @Override
    public void flush() {
      ops.add(new Operation.Skip(count));
    }

  }

  private IOperationBuilder builder = new EmptyBuilder();
  private final List<Operation> ops = Factory.newArrayList();

  /**
   * Returns the completed transformation.
   */
  public Transform flush() {
    builder.flush();
    builder = null;
    return new Transform(ops);
  }

  public void insert(String text) {
    if (!text.isEmpty()) {
      builder = builder.insert(text);
    }
  }

  public void delete(String text) {
    if (!text.isEmpty()) {
      builder = builder.delete(text);
    }
  }

  public void skip(int count) {
    Assert.that(count >= 0);
    if (count > 0) {
      builder = builder.skip(count);
    }
  }

}