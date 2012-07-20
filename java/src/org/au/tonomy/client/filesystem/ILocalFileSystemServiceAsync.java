package org.au.tonomy.client.filesystem;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Async implementation of the local file service.
 */
public interface ILocalFileSystemServiceAsync {

  /**
   * Returns the contents of a local file.
   */
  public void getContents(String path, AsyncCallback<String> callback);

  /**
   * Returns whether or not this is a directory.
   */
  public void isDirectory(String path, AsyncCallback<Boolean> callback);

  /**
   * Returns the files under the given path.
   */
  public void getChildren(String path, AsyncCallback<List<String>> callback);

}
