package org.au.tonomy.server.agent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.List;

import org.au.tonomy.shared.ot.IDocument;
import org.au.tonomy.shared.ot.Transform;
import org.au.tonomy.shared.util.Exceptions;
import org.au.tonomy.shared.util.Factory;

/**
 * The data about a file shared between all sessions.
 */
public class SharedFile {

  private final FileSystem fileSystem;
  private final String fullPath;
  private final File file;
  private IDocument contents;

  public SharedFile(FileSystem fileSystem, String fullPath) {
    this.fileSystem = fileSystem;
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
      result.add(this.fileSystem.getOrCreateFile(child.getAbsolutePath()));
    return result;
  }

  public IDocument getContents() {
    if (contents == null) {
      String text = readFileRaw();
      contents = fileSystem.getDocumentProvider().newDocument(text);
    }
    return contents;
  }

  private String readFileRaw() {
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

  public void apply(Transform transform) {
    String oldText = getContents().getText();
    String newText = transform.call(oldText);
    contents = fileSystem.getDocumentProvider().newDocument(newText);
    System.out.println(newText);
  }

  private static final Charset UTF8 = Charset.forName("UTF-8");

  public void save() {
    try {
      FileOutputStream out = new FileOutputStream(file);
      out.write(getContents().getText().getBytes(UTF8));
    } catch (IOException ioe) {
      throw Exceptions.propagate(ioe);
    }
  }

}
