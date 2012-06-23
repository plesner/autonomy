package org.au.tonomy.client.world;

import static org.au.tonomy.client.webgl.RenderingContext.ARRAY_BUFFER;
import static org.au.tonomy.client.webgl.RenderingContext.COLOR_BUFFER_BIT;
import static org.au.tonomy.client.webgl.RenderingContext.FLOAT;
import static org.au.tonomy.client.webgl.RenderingContext.FRAGMENT_SHADER;
import static org.au.tonomy.client.webgl.RenderingContext.STATIC_DRAW;
import static org.au.tonomy.client.webgl.RenderingContext.TRIANGLE_FAN;
import static org.au.tonomy.client.webgl.RenderingContext.VERTEX_SHADER;

import org.au.tonomy.client.webgl.Buffer;
import org.au.tonomy.client.webgl.Float32Array;
import org.au.tonomy.client.webgl.Program;
import org.au.tonomy.client.webgl.RenderingContext;
import org.au.tonomy.client.webgl.Shader;
import org.au.tonomy.client.webgl.UniformLocation;
import org.au.tonomy.client.webgl.util.Color;
import org.au.tonomy.client.webgl.util.Mat4;
import org.au.tonomy.client.world.shader.ShaderBundle;
import org.au.tonomy.shared.world.Hex;
import org.au.tonomy.shared.world.Hex.Corner;
import org.au.tonomy.shared.world.HexGrid;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.core.client.GWT;

/**
 * The object responsible for rendering the state of the world onto
 * the widget's canvas using WebGL.
 */
public class WorldRenderer {

  private static final ShaderBundle SHADER_BUNDLE = GWT.create(ShaderBundle.class);

  private final Canvas canvas;
  private final HexGrid grid;
  private final WorldView view = new WorldView();

  private final Buffer hexVertices;
  private final int vertexAttribLocation;
  private final UniformLocation perspectiveLocation;
  private final UniformLocation positionLocation;
  private final UniformLocation colorLocation;
  private final Mat4 perspective = Mat4.create();
  private final Mat4 position = Mat4.create();

  public WorldRenderer(Canvas canvas, HexGrid grid) {
    this.canvas = canvas;
    this.grid = grid;
    RenderingContext context = RenderingContext.forCanvas(canvas);
    Program shaderProgram = linkShaders(context);
    this.vertexAttribLocation = context.getAttribLocation(shaderProgram, "vertex");
    context.enableVertexAttribArray(vertexAttribLocation);
    this.perspectiveLocation = context.getUniformLocation(shaderProgram, "perspective");
    this.positionLocation = context.getUniformLocation(shaderProgram, "position");
    this.colorLocation = context.getUniformLocation(shaderProgram, "color");
    this.hexVertices = createHexVertices(context);
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

  /**
   * Draws the current array buffer at the specified position using
   * the given context.
   */
  private void drawArrayBuffer(RenderingContext context, double x,
      double y, double scale, Color fill, Color stroke) {
    // Move the position matrix to the desired position and scale.
    position
        .resetToIdentity()
        .translate(x, y, 0)
        .scale(scale, scale, 1);
    context.uniformMatrix4fv(positionLocation, false, position);
    context.uniform4fv(colorLocation, stroke);
    // Draw the array buffer.
    context.drawArrays(TRIANGLE_FAN, 0, 6);
    position.scale(0.95, 0.95, 1);
    context.uniformMatrix4fv(positionLocation, false, position);
    context.uniform4fv(colorLocation, fill);
    // Draw the array buffer.
    context.drawArrays(TRIANGLE_FAN, 0, 6);
  }

  public WorldView getView() {
    return this.view;
  }

  public void paint() {
    RenderingContext gl = RenderingContext.forCanvas(canvas);

    double viewportWidth = canvas.getCoordinateSpaceWidth();
    double viewportHeight = canvas.getCoordinateSpaceWidth();

    // Clear the whole canvas and reset the perspective.
    gl.viewport(0, 0, viewportWidth, viewportHeight);
    gl.clear(COLOR_BUFFER_BIT);
    perspective
        .resetPerspective(45, viewportWidth / viewportHeight, 0.1, 100.0)
        .translate(view.getCenterX(), view.getCenterY(), -16 * view.getZoom());
    gl.uniformMatrix4fv(perspectiveLocation, false, perspective);

    // Bind the hex vertices.
    gl.bindBuffer(ARRAY_BUFFER, hexVertices);
    gl.vertexAttribPointer(vertexAttribLocation, 3, FLOAT, false, 0, 0);

    Color fill = Color.create(.929, .749, .525, 1.0);
    for (Hex hex : grid) {
      drawArrayBuffer(gl, hex.getCenterX(), hex.getCenterY(), 0.95,
          fill, Color.BLACK);
    }
  }

}
