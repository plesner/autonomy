package org.au.tonomy.shared.ot;


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
  public static Pair<Transform, Transform> compose(Transform a, Transform b) {
    OperationInputStream aIn = new OperationInputStream(a);
    OperationInputStream bIn = new OperationInputStream(b);
    OperationOutputStream aOut = new OperationOutputStream();
    OperationOutputStream bOut = new OperationOutputStream();
    // First compose the operations in each tranformation.
    while (aIn.hasCurrent() && bIn.hasCurrent())
      bIn.getCurrent().xformDispatchSecond(aIn, aOut, bIn, bOut);
    // If there are more transformations left in b we flush those.
    bIn.flush(bOut, aOut);
    // And if a is the one with more those are the ones we flush.
    aIn.flush(aOut, bOut);
    return Pair.of(aOut.flush(), bOut.flush());
  }

}
