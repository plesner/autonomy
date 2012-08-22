package org.au.tonomy.shared.ot;

import java.util.List;

import com.google.gwt.thirdparty.guava.common.collect.Lists;

/**
 * A stream of operations used to build a transform.
 */
public class OperationOutputStream {

  private interface IBuilder {

    public IBuilder skip(int count);

    public IBuilder insert(String text);

    public IBuilder delete(String text);

    public void flush();

  }

  private class EmptyBuilder implements IBuilder {

    @Override
    public IBuilder skip(int count) {
      flush();
      return new SkipBuilder().skip(count);
    }

    @Override
    public IBuilder insert(String text) {
      flush();
      return new InsertBuilder().insert(text);
    }

    @Override
    public IBuilder delete(String text) {
      flush();
      return new DeleteBuilder().delete(text);
    }

    public void flush() { }

  }

  private class InsertBuilder extends EmptyBuilder {

    private final StringBuilder buf = new StringBuilder();

    @Override
    public IBuilder insert(String text) {
      buf.append(text);
      return this;
    }

    @Override
    public void flush() {
      ops.add(new Operation.Insert(buf.toString()));
    }

  }

  private class DeleteBuilder extends EmptyBuilder {

    private final StringBuilder buf = new StringBuilder();

    @Override
    public IBuilder delete(String text) {
      buf.append(text);
      return this;
    }

    @Override
    public void flush() {
      ops.add(new Operation.Delete(buf.toString()));
    }

  }

  private class SkipBuilder extends EmptyBuilder {

    private int count = 0;

    @Override
    public IBuilder skip(int count) {
      this.count += count;
      return this;
    }

    @Override
    public void flush() {
      ops.add(new Operation.Skip(count));
    }

  }

  private IBuilder builder = new EmptyBuilder();
  private final List<Operation> ops = Lists.newArrayList();

  public void push(Operation op) {
    ops.add(op);
  }

  public Transform flush() {
    builder.flush();
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
    if (count > 0) {
      builder = builder.skip(count);
    }
  }

}