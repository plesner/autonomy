package org.au.tonomy.client.widget;

import static org.au.tonomy.client.browser.RenderingContext.COLOR_BUFFER_BIT;

import java.util.List;

import org.au.tonomy.client.browser.RenderingContext;
import org.au.tonomy.client.browser.TriangleBuffer;
import org.au.tonomy.client.browser.RenderingContext.DrawMode;
import org.au.tonomy.client.presentation.ICamera;
import org.au.tonomy.client.presentation.Viewport;
import org.au.tonomy.client.webgl.util.Color;
import org.au.tonomy.client.webgl.util.IWebGL;
import org.au.tonomy.client.webgl.util.Mat4;
import org.au.tonomy.client.webgl.util.Vec3;
import org.au.tonomy.client.webgl.util.Vec4;
import org.au.tonomy.client.webmon.Counter;
import org.au.tonomy.client.webmon.Timer;
import org.au.tonomy.client.world.shader.BoardProgram;
import org.au.tonomy.client.world.shader.IShaderBundle;
import org.au.tonomy.client.world.shader.UnitProgram;
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
  private final Mat4 perspective = Mat4.create();
  private final BoardProgram boardProgram;
  private final UnitProgram unitProgram;

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
    this.boardProgram =  linkBoardProgram(context);
    this.unitProgram =  linkUnitProgram(context);
    this.innerHexStrip = newHexStrip(context, world.getGrid().getHexes(), 0.8);
    this.outerHexStrip = newHexStrip(context, world.getGrid().getHexes(), 0.9);
    this.unitVertices = newCircleFan(context, 20, 0.5);
    context.clearColor(.975, .975, .975, 1.0);
  }

  /**
   * Compiles the board shaders and links them into a program.
   */
  private BoardProgram linkBoardProgram(RenderingContext context) {
    BoardProgram result = new BoardProgram();
    result.createAndLink(context,
        SHADER_BUNDLE.getVertexShaderCommon().getText() +
        SHADER_BUNDLE.getBoardVertexShader().getText(),
        SHADER_BUNDLE.getFragmentShader().getText());
    return result;
  }

  /**
   * Compiles the board shaders and links them into a program.
   */
  private UnitProgram linkUnitProgram(final RenderingContext context) {
    return new UnitProgram() {{
      createAndLink(context,
        SHADER_BUNDLE.getVertexShaderCommon().getText() +
        SHADER_BUNDLE.getUnitVertexShader().getText(),
        SHADER_BUNDLE.getFragmentShader().getText());
    }};
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

  public void paint(WorldTrace trace, double time) {
    FRAME_RATE_COUNTER.increment();
    long start = System.currentTimeMillis();
    try {
      render(trace, time);
    } finally {
      RENDER_TIMER.record(System.currentTimeMillis() - start);
    }
  }

  private void renderBoard(WorldTrace trace, double time) {
    boardProgram.setPerspective(perspective);
    boardProgram.setLocation(ORIGIN);
    Color ground = Color.create(.929, .749, .525, 1.0);
    boardProgram.setColor(ground.adjust(Color.Adjustment.DARKER));
    boardProgram.drawTriangles(outerHexStrip);
    boardProgram.setColor(ground);
    boardProgram.drawTriangles(innerHexStrip);
  }

  private void renderUnits(WorldTrace trace, double time) {
    unitProgram.setPerspective(perspective);
    Color red = Color.create(.50, .0, .0, 1.0);
    unitProgram.setColor(red);
    for (IUnitState state : trace.getUnits(time)) {
      Hex from = state.getFrom();
      Hex to = state.getTo();
      unitProgram.setLocations(
          Vec3.create(from.getCenterX(), from.getCenterY(), 0),
          Vec3.create(to.getCenterX(), to.getCenterY(), 0),
          state.getProgress());
      unitProgram.drawTriangles(unitVertices);
    }
  }

  private static final Vec3 ORIGIN = Vec3.create(0, 0, 0);
  private void render(final WorldTrace trace, final double time) {
    RenderingContext context = webGlUtils.create3DContext(canvas);

    // Clear the whole canvas and reset the perspective.
    context.viewport(0, 0, getCanvasWidth(), getCanvasHeight());
    context.clear(COLOR_BUFFER_BIT);
    IRect bounds = viewport.getBounds();
    perspective
        .resetOrtho(bounds.getLeft(), bounds.getRight(),
            bounds.getBottom(), bounds.getTop(), -1.0, 1.0);

    // Draw the board.
    boardProgram.use(context, new Runnable() {
      @Override
      public void run() {
        renderBoard(trace, time);
      }
    });

    // Draw the units.
    unitProgram.use(context, new Runnable() {
      @Override
      public void run() {
        renderUnits(trace, time);
      }
    });
  }

}
