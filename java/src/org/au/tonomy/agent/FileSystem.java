package org.au.tonomy.agent;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.au.tonomy.shared.util.Factory;

public class FileSystem {

  private final Map<String, SharedFile> files = Factory.newHashMap();
  private final List<SharedFile> roots = Factory.newArrayList();

  public FileSystem(List<File> rootFiles) {
    for (File file : rootFiles) {
      roots.add(getOrCreateFile(file.getAbsolutePath()));
    }
  }

  SharedFile getOrCreateFile(String fullPath) {
    SharedFile current = files.get(fullPath);
    if (current == null) {
      current = new SharedFile(this, fullPath);
      files.put(fullPath, current);
    }
    return current;
  }

  /**
   * Returns this file system's root files.
   */
  public List<SharedFile> getRoots() {
    return this.roots;
  }

}
