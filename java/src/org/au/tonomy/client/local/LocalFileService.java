package org.au.tonomy.client.local;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class LocalFileService {

  private static final ILocalFileServiceAsync STUB =
      (ILocalFileServiceAsync) GWT.create(ILocalFileService.class);

  public static void getContents(String name, AsyncCallback<String> callback) {
    STUB.getLocalFile(name, callback);
  }

}
