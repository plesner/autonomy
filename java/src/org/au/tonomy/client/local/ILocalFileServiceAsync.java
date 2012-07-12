package org.au.tonomy.client.local;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Async implementation of the local file service.
 */
public interface ILocalFileServiceAsync {

  public void getLocalFile(String name, AsyncCallback<String> callback);

}
