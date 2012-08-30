package org.au.tonomy.client.world.shader;

import static org.au.tonomy.client.browser.RenderingContext.FRAGMENT_SHADER;
import static org.au.tonomy.client.browser.RenderingContext.VERTEX_SHADER;

import org.au.tonomy.client.browser.Program;
import org.au.tonomy.client.browser.RenderingContext;
import org.au.tonomy.client.browser.Shader;
import org.au.tonomy.client.browser.UniformLocation;
import org.au.tonomy.client.browser.WebGLError.ShaderSyntaxError;
import org.au.tonomy.client.webgl.util.AttributeLocation;
import org.au.tonomy.shared.util.Assert;

/**
 * A linked shader program. The program starts out empty and unlinked
 * and must be lined by calling
 * {@link #createAndLink(RenderingContext, String, String)}.
 */
public abstract class LinkedProgram {

  // A temporary context set during calls to subclass methods to avoid
  // having to pass the value around explicitly.
  private RenderingContext scopedContext;

  // The program, bound after calling createAndLink.
  private Program program;

  /**
   * Initialize and link this program.
   */
  public void createAndLink(RenderingContext context, String vertexShaderSource,
      String fragmentShaderSource) throws ShaderSyntaxError {
    Assert.isNull(program);

    // Compile the shaders first since this might throw.
    Shader fragmentShader = context.compileShader(FRAGMENT_SHADER,
        fragmentShaderSource);
    Shader vertexShader = context.compileShader(VERTEX_SHADER,
        vertexShaderSource);

    // Create and link the program.
    Program program = context.createProgram();
    context.attachShader(program, vertexShader);
    context.attachShader(program, fragmentShader);
    context.linkProgram(program);
    this.program = program;

    // Bind the locations in the subclass.
    try {
      scopedContext = context;
      bindLocations();
    } finally {
      scopedContext = null;
    }
  }

  /**
   * Install this program as the current one being used by the rendering
   * context.
   */
  public void beginUse(RenderingContext context) {
    Assert.isNull(this.scopedContext);
    this.scopedContext = context;
    context.useProgram(this.program);
    setCapabilities();
  }

  /**
   * Stop using this program.
   */
  public void endUse() {
    Assert.notNull(scopedContext);
    this.scopedContext = null;
  }

  /**
   * Installs this program for use with the context, runs the callback,
   * and the uninstalls.
   */
  public void use(RenderingContext context, Runnable thunk) {
    beginUse(context);
    try {
      thunk.run();
    } finally {
      endUse();
    }
  }

  /**
   * If subclasses need to bind any uniform or attribute locations
   * they should implement this method which will be called when it
   * is appropriate to initialize them.
   */
  protected void bindLocations() { }

  /**
   * Returns the uniform location with the given name.
   */
  protected UniformLocation getUniformLocation(String name) {
    return Assert.notNull(scopedContext.getUniformLocation(program, name));
  }

  /**
   * Returns the attribute location with the given name.
   */
  protected AttributeLocation getAttributeLocation(String name) {
    return scopedContext.getAttribLocation(program, name);
  }

  /**
   * Will be called to set the appropriate client-side capabilities
   * after a program has been set to be used.
   */
  protected void setCapabilities() { }

  protected RenderingContext getContext() {
    return this.scopedContext;
  }

}
