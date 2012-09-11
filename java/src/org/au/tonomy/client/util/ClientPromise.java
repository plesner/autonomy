package org.au.tonomy.client.util;

import org.au.tonomy.shared.util.Promise;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Various helpers for working with promises.
 */
public class ClientPromise {

  /**
   * Creates a new async callback that fulfills or fails the given
   * promise based on how it gets called back.
   */
  public static <T> AsyncCallback<T> getCallback(final Promise<T> promise) {
    return new AsyncCallback<T>() {
      @Override
      public void onSuccess(T result) {
        promise.fulfill(result);
      }
      @Override
      public void onFailure(Throwable caught) {
        promise.fail(caught);
      }
    };
  }

  /**
   * Returns a promise that fails after the given delay with the given
   * error.
   */
  public static <T> Promise<T> failAfterDelay(int delayMs, final Throwable error) {
    final Promise<T> result = Promise.newEmpty();
    Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {
      @Override
      public boolean execute() {
        result.fail(error);
        return false;
      }
    }, delayMs);
    return result;
  }

}
