package org.au.tonomy.shared.ot;

import java.util.Iterator;
import java.util.List;

import org.au.tonomy.shared.agent.pton.POperation;
import org.au.tonomy.shared.plankton.IPlanktonDatable;
import org.au.tonomy.shared.plankton.IPlanktonFactory;
import org.au.tonomy.shared.plankton.ParseError;
import org.au.tonomy.shared.util.Assert;
import org.au.tonomy.shared.util.Factory;
import org.au.tonomy.shared.util.IFunction;
import org.au.tonomy.shared.util.Pair;
/**
 * A sequence of operations that transform a document.
 */
public class Transform implements IFunction<String, String>, Iterable<Operation>, IPlanktonDatable {

  private final List<Operation> ops;
  private int inputLengthCache = -1;
  private int outputLengthCache = -1;

  public Transform(List<Operation> ops) {
    if (Assert.enableExpensiveAssertions)
      Assert.that(isNormalized(ops));
    this.ops = ops;
  }

  /**
   * Does this transform contain any operations? Note that operations
   * can be non-empty but still have an effect (like del("z") ins("z")).
   */
  public boolean isEmpty() {
    return ops.isEmpty();
  }

  /**
   * Returns true if the given operations constitute a normalized
   * transformation, that is, one where
   */
  private static boolean isNormalized(List<Operation> ops) {
    Operation.Type last = null;
    for (Operation op : ops) {
      if (op.getType() == last)
        return false;
      last = op.getType();
    }
    return true;
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
    List<Operation> invOps = Factory.newArrayList();
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
    StringBuilder buf = new StringBuilder().append("[");
    boolean first = true;
    for (Operation op : this) {
      if (first) first = false;
      else buf.append(' ');
      buf.append(op);
    }
    return buf.append("]").toString();
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

  /**
   * Returns a single transformation that has the same effect as applying
   * a and then b.
   */
  public Transform compose(Transform b) {
    Transform a = this;
    Assert.equals(b.getInputLength(), a.getOutputLength());
    TransformBuilder out = new TransformBuilder();
    OperationStream aIn = new OperationStream(a);
    OperationStream bIn = new OperationStream(b);
    while (aIn.hasCurrent())
      bIn.getCurrent().composeDispatchSecond(aIn, bIn, out);
    bIn.composeFlush(out);
    return out.flush();
  }

  /**
   * Takes two transformation, a and b, and produces (a', b') such that
   * b'(a(s)) == a'(b(s)). In other words, a' is what you need to do
   * to have the same effect as a in the case where b has already been
   * applied, and b' is what you need to do the have the same affect
   * as b in the case where a has already been applied.
   */
  public Pair<Transform, Transform> xform(Transform b) {
    Transform a = this;
    Assert.equals(a.getInputLength(), b.getInputLength());
    OperationStream aIn = new OperationStream(a);
    OperationStream bIn = new OperationStream(b);
    TransformBuilder aOut = new TransformBuilder();
    TransformBuilder bOut = new TransformBuilder();
    // First compose the operations in each tranformation.
    while (aIn.hasCurrent() && bIn.hasCurrent())
      bIn.getCurrent().xformDispatchSecond(aIn, aOut, bIn, bOut);
    // If there are more transformations left in b we flush those.
    bIn.xformFlush(bOut, aOut);
    // And if a is the one with more those are the ones we flush.
    aIn.xformFlush(aOut, bOut);
    return Pair.of(aOut.flush(), bOut.flush());
  }

  @Override
  public Object toPlanktonData(IPlanktonFactory factory) {
    return ops;
  }

  /**
   * Creates a transform from a plankton serialized object.
   */
  public static Transform unpack(List<?> list) throws ParseError {
    List<Operation> ops = Factory.newArrayList();
    for (Object op : list)
      ops.add(Operation.unpack(POperation.parse(op)));
    return new Transform(ops);
  }

}
