package org.au.tonomy.client.util;

import org.au.tonomy.shared.util.ICallback;

import com.google.gwt.user.client.Window;

public abstract class Callback<T> implements ICallback<T> {

  @Override
  public void onFailure(Throwable error) {
    Window.alert(error.getMessage());
  }

}
