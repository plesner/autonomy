package org.au.tonomy.shared.util;

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
  private List<ICallback<? super T>> callbacks;

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
  public void onResolved(ICallback<? super T> callback) {
    if (isResolved()) {
      fireCallback(callback);
    } else {
      if (callbacks == null)
        callbacks = Factory.newArrayList();
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
    List<ICallback<? super T>> pending = callbacks;
    callbacks = null;
    for (ICallback<? super T> callback : pending)
      fireCallback(callback);
  }

  /**
   * Fires a single callback.
   */
  private void fireCallback(ICallback<? super T> callback) {
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

  /**
   * Resolve the given promise when this one is resolved.
   */
  public void forwardTo(final Promise<? super T> target) {
    this.onResolved(new ICallback<T>() {
      @Override
      public void onSuccess(T value) {
        target.fulfill(value);
      }
      @Override
      public void onFailure(Throwable error) {
        target.fail(error);
      }
    });
  }

  /**
   * Returns a new promise that is the result of applying the given
   * filter to the value of this promise. Errors are passed through
   * unchanged.
   */
  public <S> Promise<S> then(final IFunction<? super T, ? extends S> filter) {
    final Promise<S> result = newEmpty();
    onResolved(new ICallback<T>() {
      @Override
      public void onSuccess(T value) {
        result.fulfill(filter.call(value));
      }
      @Override
      public void onFailure(Throwable error) {
        result.fail(error);
      }
    });
    return result;
  }

  public <S> Promise<S> lazyThen(final IFunction<? super T, ? extends Promise<? extends S>> filter) {
    final Promise<S> result = newEmpty();
    onResolved(new ICallback<T>() {
      @Override
      public void onSuccess(T value) {
        filter.call(value).forwardTo(result);
      }
      @Override
      public void onFailure(Throwable error) {
        result.fail(error);
      }
    });
    return result;
  }

}
