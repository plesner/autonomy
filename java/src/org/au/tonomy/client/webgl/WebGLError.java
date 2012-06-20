package org.au.tonomy.client.webgl;
/**
 * Signals some error in WebGL.
 */
@SuppressWarnings("serial")
public class WebGLError extends RuntimeException {

  public WebGLError(String message) {
    super(message);
  }

  public WebGLError() { }

  /**
   * Signals a shader compilation error. Would it be nicer to catch
   * these statically? Yes. But not as fun. And by "fun" I mean annoying.
   */
  public static class ShaderSyntaxError extends WebGLError {

    public ShaderSyntaxError(String logMessage) {
      super(logMessage);
    }

  }

  /**
   * Error issued if the context has problems linking a shader program.
   */
  public static class ProgramLinkError extends WebGLError {

  }

}
