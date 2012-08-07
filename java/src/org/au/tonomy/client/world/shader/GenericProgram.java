package org.au.tonomy.client.world.shader;

import static org.au.tonomy.client.webgl.RenderingContext.ARRAY_BUFFER;
import static org.au.tonomy.client.webgl.RenderingContext.FLOAT;

import org.au.tonomy.client.webgl.TriangleBuffer;
import org.au.tonomy.client.webgl.UniformLocation;
import org.au.tonomy.client.webgl.util.AttributeLocation;
import org.au.tonomy.client.webgl.util.Color;
import org.au.tonomy.client.webgl.util.Mat4;

/**
 * Linked program that binds the data shared between the different
 * shaders.
 */
public abstract class GenericProgram extends LinkedProgram {

  private AttributeLocation vertexPosition;
  private UniformLocation uM4fvPerspective;
  private UniformLocation u4fvColor;

  @Override
  protected void bindLocations() {
    super.bindLocations();
    this.vertexPosition = getAttributeLocation("aVertexPosition");
    this.uM4fvPerspective = getUniformLocation("uPerspective");
    this.u4fvColor = getUniformLocation("uColor");
  }

  public void drawTriangles(TriangleBuffer strip) {
    getContext().bindBuffer(ARRAY_BUFFER, strip);
    getContext().vertexAttribPointer(vertexPosition, 3, FLOAT, false, 0, 0);
    getContext().drawArrays(strip.getDrawMode(), 0, strip.getVertexCount());
  }

  /**
   * Sets the current perspective matrix.
   */
  public void setPerspective(Mat4 perspective) {
    getContext().uniformMatrix4fv(uM4fvPerspective, false, perspective);
  }

  /**
   * Sets the color.
   */
  public void setColor(Color color) {
    getContext().uniform4fv(u4fvColor, color.getVector());
  }

  @Override
  protected void setCapabilities() {
    super.setCapabilities();
    getContext().enableVertexAttribArray(vertexPosition);
  }

}
