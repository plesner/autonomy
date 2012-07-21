package org.au.tonomy.client.widget;

import static org.au.tonomy.client.webgl.RenderingContext.ARRAY_BUFFER;
import static org.au.tonomy.client.webgl.RenderingContext.COLOR_BUFFER_BIT;
import static org.au.tonomy.client.webgl.RenderingContext.FLOAT;
import static org.au.tonomy.client.webgl.RenderingContext.FRAGMENT_SHADER;
import static org.au.tonomy.client.webgl.RenderingContext.STATIC_DRAW;
import static org.au.tonomy.client.webgl.RenderingContext.VERTEX_SHADER;

import java.util.Arrays;
import java.util.List;

import org.au.tonomy.client.presentation.ICamera;
import org.au.tonomy.client.presentation.Viewport;
import org.au.tonomy.client.webgl.Buffer;
import org.au.tonomy.client.webgl.Float32Array;
import org.au.tonomy.client.webgl.Program;
import org.au.tonomy.client.webgl.RenderingContext;
import org.au.tonomy.client.webgl.Shader;
import org.au.tonomy.client.webgl.TriangleStrip;
import org.au.tonomy.client.webgl.UniformLocation;
import org.au.tonomy.client.webgl.util.Color;
import org.au.tonomy.client.webgl.util.Color.Adjustment;
import org.au.tonomy.client.webgl.util.IWebGL;
import org.au.tonomy.client.webgl.util.Mat4;
import org.au.tonomy.client.webgl.util.Vec4;
import org.au.tonomy.client.webgl.util.VecColor;
import org.au.tonomy.client.world.shader.IShaderBundle;
import org.au.tonomy.shared.util.Assert;
import org.au.tonomy.shared.util.IRect;
import org.au.tonomy.shared.world.Hex;
import org.au.tonomy.shared.world.Hex.Corner;
import org.au.tonomy.shared.world.World;
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

  private final TriangleStrip hexVertices;
  private final TriangleStrip unitVertices;
  private final Buffer rectVertices;
  private final int vertexAttribLocation;
  private final UniformLocation u4fvPerspective;
  private final UniformLocation u1fX;
  private final UniformLocation u1fY;
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

  public WorldRenderer(IWebGL webGlUtils, Canvas canvas, Viewport<Vec4, Mat4> viewport,
      World world) {
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
    this.u4fvFill = context.getUniformLocation(shaderProgram, "colors[0]");
    this.u4fvStroke = context.getUniformLocation(shaderProgram, "colors[1]");
    this.u1iColorSelector = context.getUniformLocation(shaderProgram, "colorSelector");
    this.hexVertices = newHexStrip(context, world.getGrid().getHexes(), 0.9);
    this.unitVertices = newHexStrip(context, Arrays.asList(world.getGrid().getHex(0, 0)), 0.5);
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
  private TriangleStrip newHexStrip(RenderingContext context, List<Hex> hexList, double s) {
    Assert.that(!hexList.isEmpty());
    TriangleStrip.Builder builder = TriangleStrip.builder(10 * hexList.size() - 3);
    for (int i = 0; i < hexList.size(); i++) {
      Hex hex = hexList.get(i);
      double x = hex.getCenterX();
      double y = hex.getCenterY();
      if (i == 0) {
        // For the very first hex we have to start the triangle strip.
        builder.start(
            x + Corner.NORTH_WEST.getX() * s,
            y + Corner.NORTH_WEST.getY() * s,
            0.0);
      }
      // Then add the four triangles that make up this hex, assuming
      // that the north west corner has already been taken care of.
      int offset = i * 10;
      builder
          .add(offset + 0,
              x + Corner.NORTH.getX() * s,
              y + Corner.NORTH.getY() * s,
              0.0)
          .add(offset + 1,
              x + Corner.NORTH_EAST.getX() * s,
              y + Corner.NORTH_EAST.getY() * s,
              0.0)
          .add(offset + 2,
              x + Corner.SOUTH_EAST.getX() * s,
              y + Corner.SOUTH_EAST.getY() * s,
              0.0)
          .add(offset + 3,
              x + Corner.NORTH_WEST.getX() * s,
              y + Corner.NORTH_WEST.getY() * s,
              0.0)
          .add(offset + 4,
              x + Corner.SOUTH_WEST.getX() * s,
              y + Corner.SOUTH_WEST.getY() * s,
              0.0)
          .add(offset + 5,
              x + Corner.SOUTH.getX() * s,
              y + Corner.SOUTH.getY() * s,
              0.0)
          .add(offset + 6,
              x + Corner.SOUTH_EAST.getX() * s,
              y + Corner.SOUTH_EAST.getY() * s,
              0.0);
      if (i < hexList.size() - 1) {
        // If there are more hexes to come we draw a degenerate triangle
        // to the north west corner of the next one.
        Hex next = hexList.get(i + 1);
        double nx = next.getCenterX();
        double ny = next.getCenterY();
        builder
            .add(offset + 7,
                x + Corner.SOUTH_EAST.getX() * s,
                y + Corner.SOUTH_EAST.getY() * s,
                0.0)
            .add(offset + 8,
                nx + Corner.NORTH_WEST.getX() * s,
                ny + Corner.NORTH_WEST.getY() * s,
                0.0)
            .add(offset + 9,
                nx + Corner.NORTH_WEST.getX() * s,
                ny + Corner.NORTH_WEST.getY() * s,
                0.0);
      }
    }
    return builder.build(context);
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
    gl.drawArrays(gl.TRIANGLE_STRIP, 0, count);
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
    fillAndStrokeArrayBuffer(gl, 0, 0, hexVertices.getVertexCount());

    // Draw the units.
    gl.bindBuffer(ARRAY_BUFFER, unitVertices);
    gl.vertexAttribPointer(vertexAttribLocation, 3, FLOAT, false, 0, 0);

    Color red = Color.create(.50, .0, .0, 1.0);
    setColors(gl, red.getVector(), Color.BLACK.getVector());
    for (IUnitState state : trace.getUnits(time)) {
      Hex from = state.getFrom();
      Hex to = state.getTo();
      double p = state.getProgress();
      double x = (to.getCenterX() * p) + (from.getCenterX() * (1 - p));
      double y = (to.getCenterY() * p) + (from.getCenterY() * (1 - p));
      fillAndStrokeArrayBuffer(gl, x, y, unitVertices.getVertexCount());
    }

    /* Draw the viewport.
    gl.bindBuffer(ARRAY_BUFFER, rectVertices);
    gl.vertexAttribPointer(vertexAttribLocation, 3, FLOAT, false, 0, 0);
    setScale(gl, bounds.getWidth(), bounds.getHeight());
    setColors(gl, Color.BLACK.getVector(), Color.BLACK.getVector());
    strokeArrayBuffer(gl, bounds.getLeft(), bounds.getBottom(), 4);
    */
  }

}
