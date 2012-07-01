package org.au.tonomy.client.widget;

import static org.au.tonomy.client.webgl.RenderingContext.ARRAY_BUFFER;
import static org.au.tonomy.client.webgl.RenderingContext.COLOR_BUFFER_BIT;
import static org.au.tonomy.client.webgl.RenderingContext.FLOAT;
import static org.au.tonomy.client.webgl.RenderingContext.FRAGMENT_SHADER;
import static org.au.tonomy.client.webgl.RenderingContext.STATIC_DRAW;
import static org.au.tonomy.client.webgl.RenderingContext.VERTEX_SHADER;

import org.au.tonomy.client.presentation.ICamera;
import org.au.tonomy.client.presentation.Viewport;
import org.au.tonomy.client.webgl.Buffer;
import org.au.tonomy.client.webgl.Float32Array;
import org.au.tonomy.client.webgl.Program;
import org.au.tonomy.client.webgl.RenderingContext;
import org.au.tonomy.client.webgl.Shader;
import org.au.tonomy.client.webgl.UniformLocation;
import org.au.tonomy.client.webgl.util.Color;
import org.au.tonomy.client.webgl.util.Color.Adjustment;
import org.au.tonomy.client.webgl.util.IWebGL;
import org.au.tonomy.client.webgl.util.Mat4;
import org.au.tonomy.client.webgl.util.Vec4;
import org.au.tonomy.client.webgl.util.VecColor;
import org.au.tonomy.client.world.shader.IShaderBundle;
import org.au.tonomy.shared.util.IRect;
import org.au.tonomy.shared.world.Hex;
import org.au.tonomy.shared.world.Hex.Corner;
import org.au.tonomy.shared.world.WorldTrace;
import org.au.tonomy.shared.world.WorldTrace.IUnitState;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.core.client.GWT;

/**
 * The object responsible for rendering the state of the world onto
 * the widget's canvas using WebGL.
 */
public class WorldRenderer implements ICamera<Vec4, Mat4> {

  private static final IShaderBundle SHADER_BUNDLE = GWT.create(IShaderBundle.class);

  private final IWebGL webGlUtils;
  private final Viewport<Vec4, Mat4> viewport;
  private final Canvas canvas;

  private final Buffer hexVertices;
  private final Buffer unitVertices;
  private final Buffer rectVertices;
  private final int vertexAttribLocation;
  private final UniformLocation u4fvPerspective;
  private final UniformLocation u1fX;
  private final UniformLocation u1fY;
  private final UniformLocation uifScaleX;
  private final UniformLocation u1fScaleY;
  private final UniformLocation u4fvFill;
  private final UniformLocation u4fvStroke;
  private final UniformLocation u1iColorSelector;
  private final Mat4 perspective = Mat4.create();

  @Override
  public double getCanvasWidth() {
    return canvas.getCoordinateSpaceWidth();
  }

  @Override
  public double getCanvasHeight() {
    return canvas.getCoordinateSpaceHeight();
  }

  public Mat4 getInversePerspective() {
    return perspective.inverse();
  }

  public WorldRenderer(IWebGL webGlUtils, Canvas canvas, Viewport<Vec4, Mat4> viewport) {
    this.webGlUtils = webGlUtils;
    this.viewport = viewport;
    this.canvas = canvas;
    RenderingContext context = webGlUtils.create3DContext(canvas);
    Program shaderProgram = linkShaders(context);
    this.vertexAttribLocation = context.getAttribLocation(shaderProgram, "vertex");
    context.enableVertexAttribArray(vertexAttribLocation);
    this.u4fvPerspective = context.getUniformLocation(shaderProgram, "perspective");
    this.u1fX = context.getUniformLocation(shaderProgram, "x");
    this.u1fY = context.getUniformLocation(shaderProgram, "y");
    this.uifScaleX = context.getUniformLocation(shaderProgram, "scaleX");
    this.u1fScaleY = context.getUniformLocation(shaderProgram, "scaleY");
    this.u4fvFill = context.getUniformLocation(shaderProgram, "colors[0]");
    this.u4fvStroke = context.getUniformLocation(shaderProgram, "colors[1]");
    this.u1iColorSelector = context.getUniformLocation(shaderProgram, "colorSelector");
    this.hexVertices = createHexVertices(context);
    this.unitVertices = createHexVertices(context);
    this.rectVertices = createRectVertices(context);
    context.clearColor(.975, .975, .975, 1.0);
  }

  /**
   * Compiles the shaders and links them into a program.
   */
  private Program linkShaders(RenderingContext context) {
    // Compile the shader scripts.
    Shader fragmentShader = context.compileShader(FRAGMENT_SHADER,
        SHADER_BUNDLE.getFragmentShader().getText());
    Shader vertexShader = context.compileShader(VERTEX_SHADER,
        SHADER_BUNDLE.getVertexShader().getText());

    // Install the shaders into the context.
    Program program = context.createProgram();
    context.attachShader(program, vertexShader);
    context.attachShader(program, fragmentShader);
    context.linkAndUseProgram(program);
    return program;
  }

  /**
   * Creates the buffer containing the hex vertices.
   */
  private Buffer createHexVertices(RenderingContext context) {
    Buffer result = context.createBuffer();
    context.bindBuffer(ARRAY_BUFFER, result);
    Float32Array vertices = Float32Array.create(
        Corner.NORTH_EAST.getX(), Corner.NORTH_EAST.getY(), 0.0,
        Corner.NORTH.getX(), Corner.NORTH.getY(), 0.0,
        Corner.NORTH_WEST.getX(), Corner.NORTH_WEST.getY(), 0.0,
        Corner.SOUTH_WEST.getX(), Corner.SOUTH_WEST.getY(), 0.0,
        Corner.SOUTH.getX(), Corner.SOUTH.getY(), 0.0,
        Corner.SOUTH_EAST.getX(), Corner.SOUTH_EAST.getY(), 0.0);
    context.bufferData(ARRAY_BUFFER, vertices, STATIC_DRAW);
    return result;
  }

  private Buffer createRectVertices(RenderingContext context) {
    Buffer result = context.createBuffer();
    context.bindBuffer(ARRAY_BUFFER, result);
    Float32Array vertices = Float32Array.create(
        1.0, 1.0, 0.0,
        0,   1.0, 0.0,
        0,   0,   0.0,
        1.0, 0,   0.0);
    context.bufferData(ARRAY_BUFFER, vertices, STATIC_DRAW);
    return result;
  }

  /**
   * Sets the vertex shader's x and y scaling factors.
   */
  private native void setScale(RenderingContext gl, double scaleX,
      double scaleY) /*-{
    gl.uniform1f(this.@org.au.tonomy.client.widget.WorldRenderer::uifScaleX, scaleX);
    gl.uniform1f(this.@org.au.tonomy.client.widget.WorldRenderer::u1fScaleY, scaleY);
  }-*/;

  private native void setColors(RenderingContext gl, VecColor fill,
      VecColor stroke) /*-{
     gl.uniform4fv(this.@org.au.tonomy.client.widget.WorldRenderer::u4fvFill, fill);
     gl.uniform4fv(this.@org.au.tonomy.client.widget.WorldRenderer::u4fvStroke, stroke);
  }-*/;

  /**
   * Draws the current array buffer at the given x, y, and scale using
   * the given stroke and fill colors and stroke ratio. The position
   * matrix is clobbered.
   *
   * This is all bundled into one native function to reduce the number
   * of gwt calls between java and javascript in hosted mode.
   */
  private native void fillAndStrokeArrayBuffer(RenderingContext gl, double x,
      double y, int count) /*-{
    gl.uniform1f(this.@org.au.tonomy.client.widget.WorldRenderer::u1fX, x);
    gl.uniform1f(this.@org.au.tonomy.client.widget.WorldRenderer::u1fY, y);
    gl.uniform1i(this.@org.au.tonomy.client.widget.WorldRenderer::u1iColorSelector, 0);
    gl.drawArrays(gl.TRIANGLE_FAN, 0, count);
    gl.uniform1i(this.@org.au.tonomy.client.widget.WorldRenderer::u1iColorSelector, 1);
    gl.drawArrays(gl.LINE_LOOP, 0, count);
  }-*/;

  private native void strokeArrayBuffer(RenderingContext gl,
      double x, double y, int count) /*-{
    gl.uniform1f(this.@org.au.tonomy.client.widget.WorldRenderer::u1fX, x);
    gl.uniform1f(this.@org.au.tonomy.client.widget.WorldRenderer::u1fY, y);
    gl.uniform1i(this.@org.au.tonomy.client.widget.WorldRenderer::u1iColorSelector, 1);
    gl.drawArrays(gl.LINE_LOOP, 0, count);
  }-*/;

  public void paint(WorldTrace trace, double time) {
    RenderingContext gl = webGlUtils.create3DContext(canvas);

    // Clear the whole canvas and reset the perspective.
    gl.viewport(0, 0, getCanvasWidth(), getCanvasHeight());
    gl.clear(COLOR_BUFFER_BIT);
    IRect bounds = viewport.getBounds();
    perspective
        .resetOrtho(bounds.getLeft() - 1, bounds.getRight() + 1,
            bounds.getBottom() - 1, bounds.getTop() + 1, -1.0, 1.0);
    gl.uniformMatrix4fv(u4fvPerspective, false, perspective);

    // Draw the hexes.
    gl.bindBuffer(ARRAY_BUFFER, hexVertices);
    gl.vertexAttribPointer(vertexAttribLocation, 3, FLOAT, false, 0, 0);

    Color ground = Color.create(.929, .749, .525, 1.0);
    setColors(gl, ground.getVector(), ground.adjust(Adjustment.DARKER).getVector());
    setScale(gl, 0.9, 0.9);
    for (Hex hex : trace.getWorld().getGrid().getHexes(bounds))
      fillAndStrokeArrayBuffer(gl, hex.getCenterX(), hex.getCenterY(), 6);

    // Draw the units.
    gl.bindBuffer(ARRAY_BUFFER, unitVertices);
    gl.vertexAttribPointer(vertexAttribLocation, 3, FLOAT, false, 0, 0);

    Color red = Color.create(.50, .0, .0, 1.0);
    setScale(gl, 0.5, 0.5);
    setColors(gl, red.getVector(), Color.BLACK.getVector());
    for (IUnitState state : trace.getUnits(time)) {
      Hex from = state.getFrom();
      Hex to = state.getTo();
      double p = state.getProgress();
      double x = (to.getCenterX() * p) + (from.getCenterX() * (1 - p));
      double y = (to.getCenterY() * p) + (from.getCenterY() * (1 - p));
      fillAndStrokeArrayBuffer(gl, x, y, 6);
    }

    // Draw the viewport.
    gl.bindBuffer(ARRAY_BUFFER, rectVertices);
    gl.vertexAttribPointer(vertexAttribLocation, 3, FLOAT, false, 0, 0);
    setScale(gl, bounds.getWidth(), bounds.getHeight());
    setColors(gl, Color.BLACK.getVector(), Color.BLACK.getVector());
    strokeArrayBuffer(gl, bounds.getLeft(), bounds.getBottom(), 4);
  }

}
