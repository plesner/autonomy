package org.au.tonomy.shared.syntax.testdata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.au.tonomy.shared.util.Assert;
import org.au.tonomy.shared.util.Exceptions;

/**
 * A script test case.
 */
public class TestScript {

  private static final String TEST_CASE_EXTENSION = ".aut";

  private final File origin;
  private final String source;

  public TestScript(File origin, String source) {
    this.origin = origin;
    this.source = source;
  }

  /**
   * Returns the file name of this script.
   */
  public String getName() {
    return origin.getName();
  }

  /**
   * Returns this test script's source code.
   */
  public String getSource() {
    return this.source;
  }

  /**
   * Returns a list of all the test cases in the testdata package.
   * This is a bit of a hack and may not work in all cases.
   */
  public static Collection<TestScript> getAll() {
    Class<TestScript> klass = TestScript.class;
    ClassLoader loader = klass.getClassLoader();
    String packageName = klass.getPackage().getName();
    String pathName = packageName.replace('.', '/');
    URL url = loader.getResource(pathName);
    Assert.notNull(url);
    File root;
    try {
      root = new File(url.toURI());
    } catch (URISyntaxException use) {
      throw Exceptions.propagate(use);
    }
    Assert.that(root.isDirectory());
    File[] files = root.listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        return name.endsWith(TEST_CASE_EXTENSION);
      }
    });
    List<TestScript> result = new ArrayList<TestScript>();
    for (File file : files) {
      String source = readFile(file);
      result.add(new TestScript(file, source));
    }
    return result;
  }

  /**
   * Isn't it wonderful that Java doesn't ship with this function built
   * in?
   */
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
