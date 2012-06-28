package org.au.tonomy.client.widget;

import static org.au.tonomy.client.webgl.RenderingContext.ARRAY_BUFFER;
import static org.au.tonomy.client.webgl.RenderingContext.COLOR_BUFFER_BIT;
import static org.au.tonomy.client.webgl.RenderingContext.FLOAT;
import static org.au.tonomy.client.webgl.RenderingContext.FRAGMENT_SHADER;
import static org.au.tonomy.client.webgl.RenderingContext.STATIC_DRAW;
import static org.au.tonomy.client.webgl.RenderingContext.VERTEX_SHADER;

import java.util.Collection;

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
import org.au.tonomy.shared.world.Hex;
import org.au.tonomy.shared.world.Hex.Corner;
import org.au.tonomy.shared.world.Unit;
import org.au.tonomy.shared.world.World;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Label;

/**
 * The object responsible for rendering the state of the world onto
 * the widget's canvas using WebGL.
 */
public class WorldRenderer implements ICamera<Vec4, Mat4> {

  private static final IShaderBundle SHADER_BUNDLE = GWT.create(IShaderBundle.class);

  private final IWebGL webGlUtils;
  private final Viewport<Vec4, Mat4> viewport;
  private final Canvas canvas;
  private final World world;
  private final Label log;

  private final Buffer hexVertices;
  private final Buffer unitVertices;
  private final Buffer rectVertices;
  private final int vertexAttribLocation;
  private final UniformLocation perspectiveLocation;
  private final UniformLocation positionLocation;
  private final UniformLocation colorLocation;
  private final Mat4 perspective = Mat4.create();
  private final Mat4 position = Mat4.create();

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

  public WorldRenderer(IWebGL webGlUtils, Canvas canvas, World world,
      Viewport<Vec4, Mat4> viewport, Label log) {
    this.webGlUtils = webGlUtils;
    this.viewport = viewport;
    this.canvas = canvas;
    this.world = world;
    this.log = log;
    RenderingContext context = webGlUtils.create3DContext(canvas);
    Program shaderProgram = linkShaders(context);
    this.vertexAttribLocation = context.getAttribLocation(shaderProgram, "vertex");
    context.enableVertexAttribArray(vertexAttribLocation);
    this.perspectiveLocation = context.getUniformLocation(shaderProgram, "perspective");
    this.positionLocation = context.getUniformLocation(shaderProgram, "position");
    this.colorLocation = context.getUniformLocation(shaderProgram, "color");
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
   * Draws the current array buffer at the given x, y, and scale using
   * the given stroke and fill colors and stroke ratio. The position
   * matrix is clobbered.
   *
   * This is all bundled into one native function to reduce the number
   * of gwt calls between java and javascript in hosted mode.
   */
  private static native void fillAndStrokeArrayBuffer(RenderingContext gl,
      double x, double y, double scale, UniformLocation posLoc, Mat4 pos,
      UniformLocation colorLoc, VecColor stroke, VecColor fill, int count) /*-{
    $wnd.mat4.identity(pos);
    $wnd.mat4.translate(pos, [x, y, 0]);
    $wnd.mat4.scale(pos, [scale, scale, 1]);
    gl.uniformMatrix4fv(posLoc, false, pos);
    gl.uniform4fv(colorLoc, fill);
    gl.drawArrays(gl.TRIANGLE_FAN, 0, count);
    gl.uniformMatrix4fv(posLoc, false, pos);
    gl.uniform4fv(colorLoc, stroke);
    gl.drawArrays(gl.LINE_LOOP, 0, count);
  }-*/;

  private static native void strokeArrayBuffer(RenderingContext gl,
      double x, double y, double scaleX, double scaleY, UniformLocation posLoc,
      Mat4 pos, UniformLocation colorLoc, VecColor stroke, int count) /*-{
    $wnd.mat4.identity(pos);
    $wnd.mat4.translate(pos, [x, y, 0]);
    $wnd.mat4.scale(pos, [scaleX, scaleY, 1]);
    gl.uniformMatrix4fv(posLoc, false, pos);
    gl.uniform4fv(colorLoc, stroke);
    gl.drawArrays(gl.LINE_LOOP, 0, count);
  }-*/;

  /**
   * Draws the current array buffer at the specified position using
   * the given context.
   */
  private void fillAndStrokeArrayBuffer(RenderingContext context, double x,
      double y, double scale, Color fill, Color stroke, int vertices) {
    fillAndStrokeArrayBuffer(context, x, y, scale, positionLocation,
        position, colorLocation, stroke.getVector(), fill.getVector(), vertices);
  }

  /**
   * Draws the current array buffer at the specified position using
   * the given context.
   */
  private void strokeArrayBuffer(RenderingContext context, double x,
      double y, double scaleX, double scaleY, Color stroke, int vertices) {
    strokeArrayBuffer(context, x, y, scaleX, scaleY, positionLocation,
        position, colorLocation, stroke.getVector(), vertices);
  }

  public void paint() {
    RenderingContext gl = webGlUtils.create3DContext(canvas);

    // Clear the whole canvas and reset the perspective.
    gl.viewport(0, 0, getCanvasWidth(), getCanvasHeight());
    gl.clear(COLOR_BUFFER_BIT);
    perspective
        .resetOrtho(viewport.getLeft() - 1, viewport.getRight() + 1,
            viewport.getBottom() - 1, viewport.getTop() + 1, -1.0, 1.0);
    gl.uniformMatrix4fv(perspectiveLocation, false, perspective);

    // Draw the hexes.
    gl.bindBuffer(ARRAY_BUFFER, hexVertices);
    gl.vertexAttribPointer(vertexAttribLocation, 3, FLOAT, false, 0, 0);

    Collection<Hex> hexes = world.getGrid().getHexes(viewport.getLeft(),
        viewport.getRight(), viewport.getBottom(), viewport.getTop());
    for (Hex hex : hexes) {
      double gRatio = 1 - ((double) hex.getG()) / (world.getHexWidth() - 1);
      double hRatio = 1 - ((double) hex.getH()) / (world.getHexHeight() - 1);
      double gAdjustment = 0.75 + (gRatio * 0.25);
      double hAdjustment = 0.75 + (hRatio * 0.25);
      Color ground = Color.create(.929, .749 * gAdjustment, .525 * hAdjustment, 1.0);
      fillAndStrokeArrayBuffer(gl, hex.getCenterX(), hex.getCenterY(),
          0.90, ground, ground.adjust(Adjustment.DARKER), 6);
    }

    // Draw the units.
    gl.bindBuffer(ARRAY_BUFFER, unitVertices);
    gl.vertexAttribPointer(vertexAttribLocation, 3, FLOAT, false, 0, 0);

    Color red = Color.create(.50, .0, .0, 1.0);
    for (Unit unit : world.getUnits()) {
      Hex hex = unit.getLocation();
      fillAndStrokeArrayBuffer(gl, hex.getCenterX(), hex.getCenterY(),
          0.5, red, Color.BLACK, 6);
    }

    // Draw the viewport.
    gl.bindBuffer(ARRAY_BUFFER, rectVertices);
    gl.vertexAttribPointer(vertexAttribLocation, 3, FLOAT, false, 0, 0);
    strokeArrayBuffer(gl, viewport.getLeft(), viewport.getBottom(),
        viewport.getWidth(), viewport.getHeight(), Color.BLACK, 4);
  }

}
