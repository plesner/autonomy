package org.au.tonomy.agent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Map;

import org.au.tonomy.shared.util.Exceptions;
import org.au.tonomy.shared.util.Factory;

public class FileSystem {

  /**
   * The data about a file shared between all sessions.
   */
  public class SharedFile {

    private final String fullPath;
    private final File file;

    public SharedFile(String fullPath) {
      this.fullPath = fullPath;
      this.file = new File(fullPath);
    }

    public String getFullPath() {
      return fullPath;
    }

    @Override
    public int hashCode() {
      return fullPath.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      } else if (!(obj instanceof SharedFile)) {
        return false;
      } else {
        return fullPath.equals(((SharedFile) obj).fullPath);
      }
    }

    public String getShortName() {
      return file.getName();
    }

    public List<SharedFile> getChildren() {
      List<SharedFile> result = Factory.newArrayList();
      for (File child : file.listFiles())
        result.add(getOrCreateFile(child.getAbsolutePath()));
      return result;
    }

    public String getContents() {
      Reader reader;
      try {
        reader = new FileReader(file);
      } catch (FileNotFoundException fnfe) {
        throw Exceptions.propagate(fnfe);
      }
      StringBuilder buf = new StringBuilder();
      char[] chunk = new char[1024];
      while (true) {
        int count;
        try {
          count = reader.read(chunk);
        } catch (IOException ioe) {
          throw Exceptions.propagate(ioe);
        }
        if (count == -1)
          break;
        buf.append(chunk, 0, count);
      }
      return buf.toString();
    }

  }

  private final Map<String, SharedFile> files = Factory.newHashMap();
  private final List<SharedFile> roots = Factory.newArrayList();

  public FileSystem(List<File> rootFiles) {
    for (File file : rootFiles) {
      roots.add(getOrCreateFile(file.getAbsolutePath()));
    }
  }

  private SharedFile getOrCreateFile(String fullPath) {
    SharedFile current = files.get(fullPath);
    if (current == null) {
      current = new SharedFile(fullPath);
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
