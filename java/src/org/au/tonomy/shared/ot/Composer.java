package org.au.tonomy.shared.ot;


import org.au.tonomy.shared.util.Assert;
import org.au.tonomy.shared.util.Pair;


/**
 * Utility for composing multiple transformations.
 */
public class Composer {

  /**
   * Takes two transformation, a and b, and produces (a', b') such that
   * b'(a(s)) == a'(b(s)). In other words, a' is what you need to do
   * to have the same effect as a in the case where b has already been
   * applied, and b' is what you need to do the have the same affect
   * as b in the case where a has already been applied.
   */
  public static Pair<Transform, Transform> xform(Transform a, Transform b) {
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

  /**
   * Returns a single transformation that has the same effect as applying
   * a and then b.
   */
  public static Transform compose(Transform a, Transform b) {
    Assert.equals(b.getInputLength(), a.getOutputLength());
    TransformBuilder out = new TransformBuilder();
    OperationStream aIn = new OperationStream(a);
    OperationStream bIn = new OperationStream(b);
    while (aIn.hasCurrent())
      bIn.getCurrent().composeDispatchSecond(aIn, bIn, out);
    bIn.composeFlush(out);
    return out.flush();
  }

}
