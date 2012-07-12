package org.au.tonomy.client.local;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
/**
 * Interface for accessing files in the local file system.
 */
@RemoteServiceRelativePath("localFileService")
public interface ILocalFileService extends RemoteService {

  /**
   * Returns the contents of a local file.
   */
  public String getLocalFile(String name);

}
