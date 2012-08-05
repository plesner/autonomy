package org.au.tonomy.client.widget;

import static org.au.tonomy.client.webgl.RenderingContext.ARRAY_BUFFER;
import static org.au.tonomy.client.webgl.RenderingContext.COLOR_BUFFER_BIT;
import static org.au.tonomy.client.webgl.RenderingContext.FLOAT;
import static org.au.tonomy.client.webgl.RenderingContext.FRAGMENT_SHADER;
import static org.au.tonomy.client.webgl.RenderingContext.VERTEX_SHADER;

import java.util.List;

import org.au.tonomy.client.presentation.ICamera;
import org.au.tonomy.client.presentation.Viewport;
import org.au.tonomy.client.webgl.Program;
import org.au.tonomy.client.webgl.RenderingContext;
import org.au.tonomy.client.webgl.RenderingContext.DrawMode;
import org.au.tonomy.client.webgl.Shader;
import org.au.tonomy.client.webgl.TriangleBuffer;
import org.au.tonomy.client.webgl.UniformLocation;
import org.au.tonomy.client.webgl.util.Color;
import org.au.tonomy.client.webgl.util.IWebGL;
import org.au.tonomy.client.webgl.util.Mat4;
import org.au.tonomy.client.webgl.util.Vec4;
import org.au.tonomy.client.webmon.Counter;
import org.au.tonomy.client.webmon.Timer;
import org.au.tonomy.client.world.shader.IShaderBundle;
import org.au.tonomy.shared.util.Assert;
import org.au.tonomy.shared.util.ExtraMath;
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

  private static final Counter FRAME_RATE_COUNTER = Counter
      .create("Frame rate")
      .setDescription("Renderer frame rate")
      .calcRate("1s");

  private static final Timer RENDER_TIMER = Timer
      .create("Render time")
      .setDescription("The time it takes to render a frame");

  private static final IShaderBundle SHADER_BUNDLE = GWT.create(IShaderBundle.class);

  private final IWebGL webGlUtils;
  private final Viewport<Vec4, Mat4> viewport;
  private final Canvas canvas;

  private final TriangleBuffer innerHexStrip;
  private final TriangleBuffer outerHexStrip;
  private final TriangleBuffer unitVertices;
  private final int vertexAttribLocation;
  private final UniformLocation u4fvPerspective;
  private final UniformLocation u1fX;
  private final UniformLocation u1fY;
  private final UniformLocation u4fvColor;
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
    this.vertexAttribLocation = context.getAttribLocation(shaderProgram, "aVertex");
    context.enableVertexAttribArray(vertexAttribLocation);
    this.u4fvPerspective = context.getUniformLocation(shaderProgram, "uPerspective");
    this.u1fX = context.getUniformLocation(shaderProgram, "uX");
    this.u1fY = context.getUniformLocation(shaderProgram, "uY");
    this.u4fvColor = context.getUniformLocation(shaderProgram, "uColor");
    this.innerHexStrip = newHexStrip(context, world.getGrid().getHexes(), 0.8);
    this.outerHexStrip = newHexStrip(context, world.getGrid().getHexes(), 0.9);
    this.unitVertices = newCircleFan(context, 20, 0.5);
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

  private TriangleBuffer newCircleFan(RenderingContext context, int segmentCount,
      double radius) {
    TriangleBuffer.TriangleArray builder = TriangleBuffer.builder(segmentCount + 1);
    builder.start(0, 0, 0);
    for (int i = 0; i <= segmentCount; i++) {
      double degree = (((double) i) / segmentCount) * ExtraMath.TAU;
      double x = Math.cos(degree) * radius;
      double y = Math.sin(degree) * radius;
      builder.add(i, x, y, 0);
    }
    return builder.toBuffer(context, DrawMode.TRIANGLE_FAN);
  }

  /**
   * Creates the buffer containing the hex vertices.
   */
  private TriangleBuffer newHexStrip(RenderingContext context, List<Hex> hexList, double s) {
    Assert.that(!hexList.isEmpty());
    TriangleBuffer.TriangleArray builder = TriangleBuffer.builder(10 * hexList.size() - 3);
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
    return builder.toBuffer(context, DrawMode.TRIANGLE_STRIP);
  }

  private void setColor(RenderingContext context, Color color) {
    context.uniform4fv(u4fvColor, color.getVector());
  }

  private void setLocation(RenderingContext context, double x, double y) {
    context.uniform1f(u1fX, x);
    context.uniform1f(u1fY, y);
  }

  private void drawTriangles(RenderingContext context, TriangleBuffer strip) {
    context.bindBuffer(ARRAY_BUFFER, strip);
    context.vertexAttribPointer(vertexAttribLocation, 3, FLOAT, false, 0, 0);
    context.drawArrays(strip.getDrawMode(), 0, strip.getVertexCount());
  }

  public void paint(WorldTrace trace, double time) {
    FRAME_RATE_COUNTER.increment();
    long start = System.currentTimeMillis();
    try {
      render(trace, time);
    } finally {
      RENDER_TIMER.record(System.currentTimeMillis() - start);
    }
  }

  private void render(WorldTrace trace, double time) {
    RenderingContext context = webGlUtils.create3DContext(canvas);

    // Clear the whole canvas and reset the perspective.
    context.viewport(0, 0, getCanvasWidth(), getCanvasHeight());
    context.clear(COLOR_BUFFER_BIT);
    IRect bounds = viewport.getBounds();
    perspective
        .resetOrtho(bounds.getLeft(), bounds.getRight(),
            bounds.getBottom(), bounds.getTop(), -1.0, 1.0);
    context.uniformMatrix4fv(u4fvPerspective, false, perspective);

    setLocation(context, 0, 0);

    // Draw the hexes.
    Color ground = Color.create(.929, .749, .525, 1.0);
    setColor(context, ground.adjust(Color.Adjustment.DARKER));
    drawTriangles(context, outerHexStrip);
    setColor(context, ground);
    drawTriangles(context, innerHexStrip);

    // Draw the units.
    Color red = Color.create(.50, .0, .0, 1.0);
    setColor(context, red);
    for (IUnitState state : trace.getUnits(time)) {
      Hex from = state.getFrom();
      Hex to = state.getTo();
      double p = state.getProgress();
      double x = (to.getCenterX() * p) + (from.getCenterX() * (1 - p));
      double y = (to.getCenterY() * p) + (from.getCenterY() * (1 - p));
      setLocation(context, x, y);
      drawTriangles(context, unitVertices);
    }
  }

}
