package org.au.tonomy.agent;

import java.util.List;
import java.util.Map;

import org.au.tonomy.shared.ot.IDocument;
import org.au.tonomy.shared.util.Factory;
import org.au.tonomy.shared.util.IJsonFactory;
import org.au.tonomy.shared.util.IJsonable;

public class Session implements IJsonable {

  private final String id;
  private final FileSystem fileSystem;
  private final Map<SharedFile, Integer> fileIds = Factory.newHashMap();
  private final Map<Integer, SessionFile> fileData = Factory.newHashMap();
  private int nextFileHandle = 0;

  public Session(String id, FileSystem fileSystem) {
    this.id = id;
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
  SessionFile getOrCreateFile(SharedFile shared) {
    Integer id = fileIds.get(shared);
    if (id == null) {
      id = nextFileHandle++;
      SessionFile data = new SessionFile(this, id, shared);
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

  public IDocument readFile(int fileId) {
    SessionFile file = fileData.get(fileId);
    return file.getShared().getContents();
  }

  @Override
  public Object toJson(IJsonFactory factory) {
    return factory.newMap().set("session", this.id);
  }

}
