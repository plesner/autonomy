package org.au.tonomy.testing;

import java.util.LinkedList;

import org.au.tonomy.shared.util.Assert;
import org.au.tonomy.shared.util.Factory;

/**
 * A utility for building mock-like objects that check that listeners
 * are called in the right way. Provides infrastructure that makes it
 * easier to build a set of expectations and check that those expectations
 * match what actually happens.
 *
 * This would have been much easier with reflection but to stay
 * compatible with GWT we have to do it the old-fashioned way.
 */
public abstract class AbstractEventChecker<C> {

  private final LinkedList<C> expected = Factory.newLinkedList();
  private C recorder;

  /**
   * Create a new object for recording expectations.
   */
  protected abstract C newRecorder();

  /**
   * Returns the next expected callback.
   */
  protected C getNextExpected() {
    Assert.that(expectsMoreEvents());
    return expected.removeFirst();
  }

  /**
   * Are there more event expectatinos queued up?
   */
  public boolean expectsMoreEvents() {
    return !expected.isEmpty();
  }

  /**
   * Adds the given checker to the sequence we expect.
   */
  protected void addExpectation(C checker) {
    Assert.notNull(checker);
    expected.addLast(checker);
  }

  /**
   * Returns an object which has the same interface as the callback
   * which records expectations.
   */
  public C getRecorder() {
    if (recorder == null)
      recorder = newRecorder();
    return recorder;
  }

}
