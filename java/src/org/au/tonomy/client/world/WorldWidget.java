package org.au.tonomy.client.world;

import static org.au.tonomy.client.webgl.RenderingContext.ARRAY_BUFFER;
import static org.au.tonomy.client.webgl.RenderingContext.COLOR_BUFFER_BIT;
import static org.au.tonomy.client.webgl.RenderingContext.DEPTH_BUFFER_BIT;
import static org.au.tonomy.client.webgl.RenderingContext.DEPTH_TEST;
import static org.au.tonomy.client.webgl.RenderingContext.FLOAT;
import static org.au.tonomy.client.webgl.RenderingContext.FRAGMENT_SHADER;
import static org.au.tonomy.client.webgl.RenderingContext.STATIC_DRAW;
import static org.au.tonomy.client.webgl.RenderingContext.TRIANGLES;
import static org.au.tonomy.client.webgl.RenderingContext.TRIANGLE_STRIP;
import static org.au.tonomy.client.webgl.RenderingContext.VERTEX_SHADER;

import org.au.tonomy.client.webgl.Buffer;
import org.au.tonomy.client.webgl.Float32Array;
import org.au.tonomy.client.webgl.Mat4;
import org.au.tonomy.client.webgl.Program;
import org.au.tonomy.client.webgl.RenderingContext;
import org.au.tonomy.client.webgl.Shader;
import org.au.tonomy.client.webgl.UniformLocation;
import org.au.tonomy.client.world.shader.ShaderBundle;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
/**
 * A widget that controls the canvas where the game is rendered.
 */
public class WorldWidget extends Composite {

  private static final ShaderBundle SHADER_BUNDLE = GWT.create(ShaderBundle.class);

  private static final WorldWidgetUiBinder BINDER = GWT.create(WorldWidgetUiBinder.class);
  interface WorldWidgetUiBinder extends UiBinder<Widget, WorldWidget> { }

  @UiField Canvas canvas;

  private Buffer triangleVertexPositionBuffer;
  private Buffer squareVertexPositionBuffer;
  private int vertexPosition;
  private UniformLocation pMatrixUniform;
  private UniformLocation mvMatrixUniform;
  private Mat4 pMatrix = Mat4.create();
  private Mat4 mvMatrix = Mat4.create();

  public WorldWidget() {
    initWidget(BINDER.createAndBindUi(this));
    render();
    this.addDomHandler(new MouseUpHandler() {
      @Override
      public void onMouseUp(MouseUpEvent event) {
        render();
      }
    }, MouseUpEvent.getType());
  }

  private void render() {
    RenderingContext gl = RenderingContext.forCanvas(canvas);
    initShaders(gl);
    initBuffers(gl);

    gl.clearColor(1.0, 1.0, 1.0, 1.0);
    gl.enable(DEPTH_TEST);

    drawScene(gl);
  }

  private void initShaders(RenderingContext gl) {
    // Compile the shader scripts.
    Shader fragmentShader = gl.compileShader(FRAGMENT_SHADER,
        SHADER_BUNDLE.getFragmentShader().getText());
    Shader vertexShader = gl.compileShader(VERTEX_SHADER,
        SHADER_BUNDLE.getVertexShader().getText());

    // Install the shaders into the context.
    Program shaderProgram = gl.createProgram();
    gl.attachShader(shaderProgram, vertexShader);
    gl.attachShader(shaderProgram, fragmentShader);
    gl.linkAndUseProgram(shaderProgram);

    vertexPosition = gl.getAttribLocation(shaderProgram, "aVertexPosition");
    gl.enableVertexAttribArray(vertexPosition);

    pMatrixUniform = gl.getUniformLocation(shaderProgram, "uPMatrix");
    mvMatrixUniform = gl.getUniformLocation(shaderProgram, "uMVMatrix");
  }

  private void initBuffers(RenderingContext gl) {
    // Initialize buffers.
    triangleVertexPositionBuffer = gl.createBuffer();
    gl.bindBuffer(ARRAY_BUFFER, triangleVertexPositionBuffer);
    Float32Array vertices = Float32Array.create(
        0.0,  1.0,  0.0,
        -1.0, -1.0,  0.0,
        1.0, -1.0,  0.0);
    gl.bufferData(ARRAY_BUFFER, vertices, STATIC_DRAW);

    squareVertexPositionBuffer = gl.createBuffer();
    gl.bindBuffer(ARRAY_BUFFER, squareVertexPositionBuffer);
    vertices = Float32Array.create(
        1.0,  1.0,  0.0,
        -1.0,  1.0,  0.0,
         1.0, -1.0,  0.0,
        -1.0, -1.0,  0.0);
    gl.bufferData(ARRAY_BUFFER, vertices, STATIC_DRAW);
  }

  private void setMatrixUniforms(RenderingContext gl) {
    gl.uniformMatrix4fv(pMatrixUniform, false, pMatrix);
    gl.uniformMatrix4fv(mvMatrixUniform, false, mvMatrix);
  }

  private void drawScene(RenderingContext gl) {
    double viewportWidth = canvas.getCoordinateSpaceWidth();
    double viewportHeight = canvas.getCoordinateSpaceWidth();

    gl.viewport(0, 0, viewportWidth, viewportHeight);
    gl.clear(COLOR_BUFFER_BIT | DEPTH_BUFFER_BIT);

    pMatrix.resetPerspective(45, viewportWidth / viewportHeight, 0.1, 100.0);
    mvMatrix.resetToIdentity().translate(-1.5, 0.0, -7.0);

    gl.bindBuffer(ARRAY_BUFFER, triangleVertexPositionBuffer);
    gl.vertexAttribPointer(vertexPosition, 3, FLOAT, false, 0, 0);
    setMatrixUniforms(gl);
    gl.drawArrays(TRIANGLES, 0, 3);

    mvMatrix.translate(3.0, 0.0, 0.0);
    gl.bindBuffer(ARRAY_BUFFER, squareVertexPositionBuffer);
    gl.vertexAttribPointer(vertexPosition, 3, FLOAT, false, 0, 0);
    setMatrixUniforms(gl);
    gl.drawArrays(TRIANGLE_STRIP, 0, 4);
  }

  @UiFactory
  public Canvas createCanvas() {
    return Canvas.createIfSupported();
  }

}
