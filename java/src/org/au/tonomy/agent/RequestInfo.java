package org.au.tonomy.agent;

import java.util.regex.Matcher;

import javax.servlet.http.HttpServletRequest;

import org.au.tonomy.shared.util.Assert;

/**
 * A collection of information about a request.
 */
public class RequestInfo {

  private final String path;
  private final HttpServletRequest request;
  private final Matcher matcher;

  public RequestInfo(String path, HttpServletRequest request, Matcher matcher) {
    this.path = path;
    this.request = request;
    this.matcher = matcher;
  }

  /**
   * Returns the requested path with the parameters stripped.
   */
  public String getPath() {
    return this.path;
  }

  /**
   * Returns the index'th group in the regexp match that selected the
   * end point that's handling this request.
   */
  public String getGroup(int index) {
    Assert.that(index <= matcher.groupCount());
    return matcher.group(index);
  }

  /**
   * Returns the value of a parameter or, if it hasn't been specified,
   * the given default value.
   */
  public String getParameter(String name, String defawlt) {
    String value = request.getParameter(name);
    return (value == null) ? defawlt : value;
  }

}