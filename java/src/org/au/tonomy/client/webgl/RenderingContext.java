package org.au.tonomy.client.webgl;

import org.au.tonomy.client.webgl.WebGLError.ProgramLinkError;
import org.au.tonomy.client.webgl.WebGLError.ShaderSyntaxError;
import org.au.tonomy.client.webgl.util.AttributeLocation;
import org.au.tonomy.client.webgl.util.Mat4;
import org.au.tonomy.client.webgl.util.Vec3;
import org.au.tonomy.client.webgl.util.Vec4;

import com.google.gwt.core.client.JavaScriptObject;
/**
 * Wrapper around a GL context. Modeled on the WebGL IDL spec at
 * http://www.khronos.org/registry/webgl/specs/latest/webgl.idl.
 */
public class RenderingContext extends JavaScriptObject {

  public static final int FRAGMENT_SHADER = 0x8B30;
  public static final int VERTEX_SHADER = 0x8B31;

  public static final int COMPILE_STATUS = 0x8B81;
  public static final int LINK_STATUS = 0x8B82;
  public static final int ARRAY_BUFFER = 0x8892;
  public static final int STATIC_DRAW = 0x88E4;

  public static final int DEPTH_BUFFER_BIT = 0x00000100;
  public static final int STENCIL_BUFFER_BIT = 0x00000400;
  public static final int COLOR_BUFFER_BIT = 0x00004000;

  public static final int CULL_FACE = 0x0B44;
  public static final int BLEND = 0x0BE2;
  public static final int DITHER = 0x0BD0;
  public static final int STENCIL_TEST = 0x0B90;
  public static final int DEPTH_TEST = 0x0B71;
  public static final int SCISSOR_TEST = 0x0C11;
  public static final int POLYGON_OFFSET_FILL = 0x8037;
  public static final int SAMPLE_ALPHA_TO_COVERAGE = 0x809E;
  public static final int SAMPLE_COVERAGE = 0x80A0;

  public static final int BYTE = 0x1400;
  public static final int UNSIGNED_BYTE = 0x1401;
  public static final int SHORT = 0x1402;
  public static final int UNSIGNED_SHORT = 0x1403;
  public static final int INT = 0x1404;
  public static final int UNSIGNED_INT = 0x1405;
  public static final int FLOAT = 0x1406;

  public enum DrawMode {

    POINTS(0x0000),
    LINES(0x0001),
    LINE_LOOP(0x0002),
    LINE_STRIP(0x0003),
    TRIANGLES(0x0004),
    TRIANGLE_STRIP(0x0005),
    TRIANGLE_FAN(0x0006);

    private final int code;

    private DrawMode(int code) {
      this.code = code;
    }

    public int getCode() {
      return code;
    }

  }

  protected RenderingContext() { }

  /**
   * Creates a new program.
   */
  public final native Program createProgram() /*-{
    return this.createProgram();
  }-*/;

  /**
   * Creates a new shader.
   */
  public final native Shader createShader(int type) /*-{
    return this.createShader(type);
  }-*/;

  /**
   * Attaches the given source to the given shader.
   */
  public final native void shaderSource(Shader shader, String source) /*-{
    this.shaderSource(shader, source);
  }-*/;

  /**
   * Compiles the given shader.
   */
  public final native void compileShader(Shader shader) /*-{
    this.compileShader(shader);
  }-*/;

  /**
   * Returns the named boolean parameter of the given shader.
   */
  public final native boolean getBooleanShaderParameter(Shader shader, int pname) /*-{
    return this.getShaderParameter(shader, pname);
  }-*/;

  /**
   * Returns the named boolean parameter of the given shader.
   */
  public final native boolean getBooleanProgramParameter(Program program, int pname) /*-{
    return this.getProgramParameter(program, pname);
  }-*/;

  /**
   * Returns the contents of the shader info log.
   */
  public final native String getShaderInfoLog(Shader shader) /*-{
    return this.getShaderInfoLog(shader);
  }-*/;

  /**
   * Compiles the given source into a shader of the specified type,
   * throwing an exception if there was an error.
   */
  public final Shader compileShader(int type, String source) throws ShaderSyntaxError {
    Shader shader = createShader(type);
    shaderSource(shader, source);
    compileShader(shader);
    if (!getBooleanShaderParameter(shader, COMPILE_STATUS)) {
      throw new ShaderSyntaxError(getShaderInfoLog(shader));
    } else {
      return shader;
    }
  }

  /**
   * Links and uses the given program, throwing a link error if there
   * was a problem.
   */
  public final void linkAndUseProgram(Program program) throws ProgramLinkError {
    linkProgram(program);
    if (!getBooleanProgramParameter(program, LINK_STATUS)) {
      throw new ProgramLinkError();
    } else {
      useProgram(program);
    }
  }

  /**
   * Links the given program.
   */
  public final native void linkProgram(Program program) /*-{
    this.linkProgram(program);
  }-*/;

  /**
   * Uses the given program.
   */
  public final native void useProgram(Program program) /*-{
    this.useProgram(program);
  }-*/;

  /**
   * Attaches a shader object to a program object.
   */
  public final native void attachShader(Program program, Shader shader) /*-{
    this.attachShader(program, shader);
  }-*/;

  /**
   * Returns the generic attribute location that the attribute variable
   * named name was bound to when the program object named program was
   * last linked.
   */
  public final AttributeLocation getAttribLocation(Program program, String name) {
    return new AttributeLocation(getAttribLocationIndex(program, name));
  }

  /**
   * Returns the raw attribute index that the attribute variable
   * named name was bound to when the program object named program was
   * last linked.
   */
  private final native int getAttribLocationIndex(Program program, String name) /*-{
    return this.getAttribLocation(program, name);
  }-*/;

  /**
   * Enable the vertex attribute at index as an array.
   */
  public final native void enableVertexAttribArray(AttributeLocation location) /*-{
    this.enableVertexAttribArray(location.@org.au.tonomy.client.webgl.util.AttributeLocation::getIndex());
  }-*/;

  /**
   * Return a new WebGLUniformLocation that represents the location of
   * a specific uniform variable within a program object. The return
   * value is null if name does not correspond to an active uniform
   * variable in the passed program.
   */
  public final native UniformLocation getUniformLocation(Program program,
      String name) /*-{
    return this.getUniformLocation(program, name);
  }-*/;

  /**
   * Create a WebGLBuffer object and initialize it with a buffer object
   * name as if by calling glGenBuffers.
   */
  public final native <B extends Buffer> B createBuffer() /*-{
    return this.createBuffer();
  }-*/;

  /**
   * Binds the given WebGLBuffer object to the given binding point
   * (target), either ARRAY_BUFFER or ELEMENT_ARRAY_BUFFER. If the
   * buffer is null then any buffer currently bound to this target is
   * unbound. A given WebGLBuffer object may only be bound to one of
   * the ARRAY_BUFFER or ELEMENT_ARRAY_BUFFER target in its lifetime.
   */
  public final native void bindBuffer(int target, Buffer buffer) /*-{
    this.bindBuffer(target, buffer);
  }-*/;

  /**
   * Set the size of the currently bound WebGLBuffer object for the
   * passed target to the size of the passed data, then write the
   * contents of data to the buffer object.
   */
  public final native void bufferData(int target, ArrayBufferView data, int usage) /*-{
    this.bufferData(target, data, usage);
  }-*/;

  /**
   * Specifies viewport transformation parameters.
   */
  public final native void viewport(double x, double y, double w, double h) /*-{
    this.viewport(x, y, w, h);
  }-*/;

  /**
   * Clears the buffer.
   */
  public final native void clear(int mask) /*-{
    this.clear(mask);
  }-*/;

  /**
   * Assign the WebGLBuffer object currently bound to the ARRAY_BUFFER
   * target to the vertex attribute at the passed index. Size is number
   * of components per attribute. Stride and offset are in units of bytes.
   */
  public final native void vertexAttribPointer(AttributeLocation attrib,
      int size, int type, boolean normalized, int stride, int offset) /*-{
    this.vertexAttribPointer(attrib.@org.au.tonomy.client.webgl.util.AttributeLocation::getIndex(),
        size, type, normalized, stride, offset);
  }-*/;

  /**
   * Sets the specified uniform or uniforms to the values provided.
   */
  public final native void uniformMatrix4fv(UniformLocation location, boolean transpose,
      Mat4 matrix) /*-{
    this.uniformMatrix4fv(location, transpose, matrix);
  }-*/;

  /**
   * Sets the specified uniform or uniforms to the values provided.
   */
  public final native void uniform4fv(UniformLocation location, Vec4 vector) /*-{
    this.uniform4fv(location, vector);
  }-*/;

  /**
   * Sets the specified uniform or uniforms to the values provided.
   */
  public final native void uniform3fv(UniformLocation location, Vec3 vector) /*-{
    this.uniform3fv(location, vector);
  }-*/;

  /**
   * Sets the specified uniform to the value provided.
   */
  public final native void uniform1f(UniformLocation location, double value) /*-{
    this.uniform1f(location, value);
  }-*/;

  /**
   * Render to the drawing buffer.
   */
  public final native void drawArrays(DrawMode mode, int first, int count) /*-{
    this.drawArrays(mode.@org.au.tonomy.client.webgl.RenderingContext.DrawMode::code,
        first, count);
  }-*/;

  /**
   * Sets the clear color.
   */
  public final native void clearColor(double red, double green, double blue, double alpha) /*-{
    this.clearColor(red, green, blue, alpha);
  }-*/;

  /**
   * Enables the specified capability.
   */
  public final native void enable(int cap) /*-{
    this.enable(cap);
  }-*/;

}
