package org.au.tonomy.agent;

import java.util.List;
import java.util.Map;

import org.au.tonomy.agent.FileSystem.SharedFile;
import org.au.tonomy.agent.Json.JsonMap;
import org.au.tonomy.shared.ot.IJsonable;
import org.au.tonomy.shared.util.Factory;

public class SessionData {

  /**
   * The file information specific to a single session.
   */
  private class SessionFile implements IJsonable {

    private final int id;
    private final SharedFile shared;

    public SessionFile(int id, SharedFile shared) {
      this.id = id;
      this.shared = shared;
    }

    @Override
    public Object toJson() {
      return new JsonMap() {{
        put("id", id);
        put("path", shared.getFullPath());
        put("name", shared.getShortName());
      }};
    }

    public List<SessionFile> listFiles() {
      List<SessionFile> result = Factory.newArrayList();
      for (SharedFile child : shared.getChildren())
        result.add(getOrCreateFile(child));
      return result;
    }

    public SharedFile getShared() {
      return shared;
    }

  }

  private final FileSystem fileSystem;
  private final Map<SharedFile, Integer> fileIds = Factory.newHashMap();
  private final Map<Integer, SessionFile> fileData = Factory.newHashMap();
  private int nextFileHandle = 0;

  public SessionData(FileSystem fileSystem) {
    this.fileSystem = fileSystem;
  }

  /**
   * Returns this session's root files.
   */
  public List<SessionFile> getRoots() {
    List<SessionFile> result = Factory.newArrayList();
    for (SharedFile shared : fileSystem.getRoots())
      result.add(getOrCreateFile(shared));
    return result;
  }

  /**
   * Returns a file data object for the given path. If none exists
   * one is created.
   */
  private SessionFile getOrCreateFile(SharedFile shared) {
    Integer id = fileIds.get(shared);
    if (id == null) {
      id = nextFileHandle++;
      SessionFile data = new SessionFile(id, shared);
      fileIds.put(shared, id);
      fileData.put(id, data);
      return data;
    } else {
      return fileData.get(id);
    }
  }

  /**
   * Returns the files under the directory with the given id.
   */
  public List<SessionFile> listFiles(int fileId) {
    SessionFile file = fileData.get(fileId);
    return file.listFiles();
  }

  public String readFile(int fileId) {
    SessionFile file = fileData.get(fileId);
    return file.getShared().getContents();
  }

}
