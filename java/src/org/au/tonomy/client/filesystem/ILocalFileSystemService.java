package org.au.tonomy.client.filesystem;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
/**
 * Interface for accessing files in the local file system.
 */
@RemoteServiceRelativePath("localFileSystem")
public interface ILocalFileSystemService extends RemoteService {

  /**
   * Returns the contents of a local file.
   */
  public String getContents(String path);

  /**
   * Returns whether or not the given path is a directory.
   */
  boolean isDirectory(String path);

  /**
   * Returns the files under the given path.
   */
  List<String> getChildren(String path);

}
