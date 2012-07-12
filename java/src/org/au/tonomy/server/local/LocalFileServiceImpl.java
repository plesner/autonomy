package org.au.tonomy.server.local;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.au.tonomy.client.local.ILocalFileService;
import org.au.tonomy.shared.util.Assert;
import org.au.tonomy.shared.util.Exceptions;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
/**
 * Default implementation of the local file service.
 */
@SuppressWarnings("serial")
public class LocalFileServiceImpl extends RemoteServiceServlet implements ILocalFileService {

  @Override
  public String getLocalFile(String name) {
    File file = new File(name);
    Assert.that(file.exists() && file.isFile());
    return readFile(file);
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
