package org.au.tonomy.shared.util;

import java.util.ArrayList;
import java.util.List;

/**
 * A deferred value.
 */
public class Promise<T> {

  private enum State {
    EMPTY,
    FAILED,
    SUCCEEDED
  }

  private State state = State.EMPTY;
  private T value;
  private Throwable error;
  private List<ICallback<T>> callbacks;

  /**
   * Sets the value of this promise. If it has already been resolved
   * nothing happens.
   */
  public void fulfill(T value) {
    if (isResolved())
      return;
    this.value = value;
    this.state = State.SUCCEEDED;
    firePendingCallbacks();
  }

  /**
   * Fails this promise. If it has already been resolved nothing
   * happens.
   */
  public void fail(Throwable error) {
    if (isResolved())
      return;
    this.error = error;
    this.state = State.FAILED;
    firePendingCallbacks();
  }

  /**
   * Has this promise been given its value?
   */
  public boolean isResolved() {
    return state != State.EMPTY;
  }

  /**
   * Adds a listener to the set that should be called when this promise
   * is resolved. If it has already been resolved the callback is
   * called immediately.
   */
  public void onResolved(ICallback<T> callback) {
    if (isResolved()) {
      fireCallback(callback);
    } else {
      if (callbacks == null)
        callbacks = new ArrayList<ICallback<T>>();
      callbacks.add(callback);
    }
  }

  /**
   * Clears and fires the list of callbacks.
   */
  private void firePendingCallbacks() {
    Assert.that(isResolved());
    if (callbacks == null)
      return;
    List<ICallback<T>> pending = callbacks;
    callbacks = null;
    for (ICallback<T> callback : pending)
      fireCallback(callback);
  }

  /**
   * Fires a single callback.
   */
  private void fireCallback(ICallback<T> callback) {
    if (state == State.SUCCEEDED) {
      callback.onSuccess(value);
    } else {
      callback.onFailure(error);
    }
  }

  /**
   * Creates a new empty promise.
   */
  public static <T> Promise<T> newEmpty() {
    return new Promise<T>();
  }

}
