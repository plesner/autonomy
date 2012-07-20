package org.au.tonomy.client.filesystem;

import java.util.ArrayList;
import java.util.List;

import org.au.tonomy.client.util.PromiseUtil;
import org.au.tonomy.shared.util.Promise;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
/**
 * A client-side representation of a file in the local file system.
 */
public class LocalFile {

  private static final ILocalFileSystemServiceAsync STUB =
      (ILocalFileSystemServiceAsync) GWT.create(ILocalFileSystemService.class);

  private final String path;

  private LocalFile(String path) {
    this.path = path;
  }

  /**
   * Returns the contents of this file.
   */
  public Promise<String> getContents() {
    Promise<String> result = Promise.newEmpty();
    STUB.getContents(path, PromiseUtil.getCallback(result));
    return result;
  }

  /**
   * Is this a directory?
   */
  public Promise<Boolean> isDirectory() {
    Promise<Boolean> result = Promise.newEmpty();
    STUB.isDirectory(path, PromiseUtil.getCallback(result));
    return result;
  }

  /**
   * Returns a list of the children of this directory.
   */
  public Promise<List<LocalFile>> getChildren() {
    final Promise<List<LocalFile>> result = Promise.newEmpty();
    STUB.getChildren(path, new AsyncCallback<List<String>>() {
      @Override
      public void onSuccess(List<String> names) {
        List<LocalFile> files = new ArrayList<LocalFile>();
        for (String name : names)
          files.add(forPath(name));
        result.fulfill(files);
      }
      @Override
      public void onFailure(Throwable caught) {
        result.fail(caught);
      }
    });
    return result;
  }

  @Override
  public String toString() {
    return "a File { " + path + " }";
  }

  /**
   * Creates a new local file with the given path. The file may or may
   * not exist.
   */
  public static LocalFile forPath(String path) {
    return new LocalFile(path);
  }

}
