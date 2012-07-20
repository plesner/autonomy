package org.au.tonomy.server.filesystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

import org.au.tonomy.client.filesystem.ILocalFileSystemService;
import org.au.tonomy.shared.util.Assert;
import org.au.tonomy.shared.util.Exceptions;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
/**
 * Default implementation of the local file service.
 */
@SuppressWarnings("serial")
public class LocalFileSystemServiceImpl extends RemoteServiceServlet implements ILocalFileSystemService {

  @Override
  public String getContents(String path) {
    File file = new File(path);
    Assert.that(file.exists() && file.isFile());
    return readFile(file);
  }

  @Override
  public boolean isDirectory(String path) {
    return new File(path).isDirectory();
  }

  @Override
  public List<String> getChildren(String path) {
    return Arrays.asList(new File(path).list());
  }

  private static String readFile(File file) {
    Assert.that(file.isFile());
    byte[] bytes = new byte[(int) file.length()];
    try {
      new FileInputStream(file).read(bytes);
    } catch (IOException ioe) {
      throw Exceptions.propagate(ioe);
    }
    String result = null;
    try {
      result = new String(bytes, "UTF8");
    } catch (UnsupportedEncodingException uee) {
      throw Exceptions.propagate(uee);
    }
    return result;
  }

}
