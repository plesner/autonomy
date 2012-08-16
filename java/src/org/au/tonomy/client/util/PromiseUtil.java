package org.au.tonomy.client.util;

import org.au.tonomy.shared.util.Promise;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Various helpers for working with promises.
 */
public class PromiseUtil {

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
   * Wrapper around JSON.parse.
   */
  public static native JavaScriptObject parseJson(String str) /*-{
    return JSON.parse(str);
  }-*/;

}
