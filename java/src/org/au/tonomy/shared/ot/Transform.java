package org.au.tonomy.shared.ot;

import java.util.Iterator;
import java.util.List;

import org.au.tonomy.shared.util.Assert;
import org.au.tonomy.shared.util.IFunction;

import com.google.gwt.thirdparty.guava.common.collect.Lists;
/**
 * A sequence of operations that transform a document.
 */
public class Transform implements IFunction<String, String>, Iterable<Operation> {

  private final List<Operation> ops;
  private int inputLengthCache = -1;
  private int outputLengthCache = -1;

  public Transform(List<Operation> ops) {
    Assert.that(!ops.isEmpty());
    this.ops = ops;
  }

  @Override
  public Iterator<Operation> iterator() {
    return ops.iterator();
  }

  /**
   * Returns a transformation that reverses the effect of applying this
   * transformation.
   */
  public Transform getInverse() {
    List<Operation> invOps = Lists.newArrayList();
    for (Operation op : this)
      invOps.add(op.getInverse());
    return new Transform(invOps);
  }

  /**
   * Returns the number of characters in the output of this transform.
   */
  public int getOutputLength() {
    if (outputLengthCache == -1) {
      int value = 0;
      for (Operation op : this)
        value += op.getOutputLength();
      outputLengthCache = value;
    }
    return outputLengthCache;
  }

  /**
   * Returns the required input size to this transform.
   */
  public int getInputLength() {
    if (inputLengthCache == -1) {
      int value = 0;
      for (Operation op : this)
        value += op.getInputLength();
      inputLengthCache = value;
    }
    return inputLengthCache;
  }

  /**
   * Returns the list of operations that makes up this transformation.
   */
  public List<Operation> getOperations() {
    return ops;
  }

  @Override
  public String toString() {
    return "<Transform: " + ops + ">";
  }

  @Override
  public int hashCode() {
    return ops.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof Transform)) {
      return false;
    } else {
      return ops.equals(((Transform) obj).ops);
    }
  }

  /**
   * Applies this transformation to a string.
   */
  @Override
  public String call(String str) {
    Assert.equals(getInputLength(), str.length());
    StringBuilder out = new StringBuilder();
    StringInput in = new StringInput(str);
    for (Operation op : this)
      op.apply(in, out);
    Assert.that(in.isDone());
    return out.toString();
  }

}
