package org.au.tonomy.shared.util;
/**
 * A generic success/failure callback used when working with promises.
 */
public interface ICallback<T> {

  /**
   * Called when an operation succeeds.
   */
  public void onSuccess(T value);

  /**
   * Called when an operation fails.
   */
  public void onFailure(Throwable error);

}
