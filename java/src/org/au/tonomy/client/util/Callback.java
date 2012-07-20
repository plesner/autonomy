package org.au.tonomy.client.util;

import org.au.tonomy.shared.util.Exceptions;
import org.au.tonomy.shared.util.ICallback;

public abstract class Callback<T> implements ICallback<T> {

  @Override
  public void onFailure(Throwable error) {
    Exceptions.propagate(error);
  }

}
