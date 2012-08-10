package org.au.tonomy.shared.syntax.testdata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.au.tonomy.shared.util.Assert;
import org.au.tonomy.shared.util.Exceptions;
import org.au.tonomy.shared.util.Factory;
import org.au.tonomy.shared.util.Pair;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

/**
 * A script test case.
 */
public class TestScript {

  /**
   * A single section within a multi-section test case.
   */
  public static class Section {

    private static final RegExp SYNTAX_ERROR_MARKER = RegExp.compile("!SYNTAX\\s+\"([^\"]+)\"");
    private final String name;
    private final String source;

    public Section(String name, String source) {
      this.name = name;
      this.source = source;
    }

    public String getName() {
      return name;
    }

    public String getSource() {
      return source;
    }

    /**
     * Are there any markers in this test script indication that we
     * expect it to fail?
     */
    public boolean expectSuccess() {
      return getExpectedOffendingToken() == null;
    }

    public String getExpectedOffendingToken() {
      MatchResult result = SYNTAX_ERROR_MARKER.exec(source);
      return (result == null) ? null : result.getGroup(1);
    }

  }

  private static final String TEST_CASE_EXTENSION = ".aut";
  private static final RegExp SECTION_HEADER = RegExp.compile("#\\{ ---([^-]+)--- \\}#", "g");

  private final File origin;
  private final String source;

  public TestScript(File origin, String source) {
    this.origin = origin;
    this.source = source;
  }

  public List<Section> getSections() {
    int index = 0;
    List<Pair<String, Integer>> headers = Factory.newArrayList();
    MatchResult match = SECTION_HEADER.exec(source);
    while (match != null) {
      String header = match.getGroup(0);
      String name = match.getGroup(1).trim();
      int matchIndex = source.indexOf(header, index);
      headers.add(Pair.of(name, matchIndex));
      index = matchIndex + header.length();
      SECTION_HEADER.setLastIndex(index);
      match = SECTION_HEADER.exec(source);
    }
    if (headers.size() == 0) {
      return Collections.singletonList(new Section(getName(), source));
    } else {
      List<Section> result = Factory.newArrayList();
      for (int i = 0; i < headers.size(); i++) {
        Pair<String, Integer> header = headers.get(i);
        int end;
        if (i == headers.size() - 1) {
          end = source.length();
        } else {
          end = headers.get(i + 1).getSecond();
        }
        String data = source.substring(header.getSecond(), end);
        result.add(new Section(header.getFirst(), data));
      }
      return result;
    }
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
    List<TestScript> result = Factory.newArrayList();
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
