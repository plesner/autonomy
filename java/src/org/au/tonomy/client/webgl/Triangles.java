package org.au.tonomy.client.webgl;

import static org.au.tonomy.client.webgl.RenderingContext.ARRAY_BUFFER;
import static org.au.tonomy.client.webgl.RenderingContext.STATIC_DRAW;

import org.au.tonomy.client.webgl.RenderingContext.DrawMode;

/**
 * A buffer that contains data for a triangle strip.
 */
public class Triangles extends Buffer {

  protected Triangles() { }

  /**
   * Initializes the vertex count field.
   */
  private final native void initVertexCount(int count) /*-{
    this.vertexCount = count;
  }-*/;

  /**
   * Initializes the draw mode field.
   */
  private final native void initDrawMode(DrawMode mode) /*-{
    this.drawMode = mode;
  }-*/;

  /**
   * Returns the number of vertices stored in this strip.
   */
  public final native int getVertexCount() /*-{
    return this.vertexCount;
  }-*/;

  /**
   * Returns the draw mode of this set of triangles.
   */
  public final native DrawMode getDrawMode() /*-{
    return this.drawMode;
  }-*/;

  /**
   * A utility for constructing a triangle strip.
   */
  public static class Builder extends Float32Array {

    protected Builder() { }

    /**
     * Sets the coordinates of the start point.
     */
    public final native Builder start(double x, double y, double z) /*-{
      this[0] = x;
      this[1] = y;
      this[2] = z;
      return this;
    }-*/;

    /**
     * Sets the coordinates of the index'th triangle.
     */
    public final native Builder add(int index, double x, double y, double z) /*-{
      var offset = 3 + (3 * index);
      this[offset + 0] = x;
      this[offset + 1] = y;
      this[offset + 2] = z;
      return this;
    }-*/;

    /**
     * Builds a triangle strip containing the data from this builder.
     */
    public final Triangles build(RenderingContext context, DrawMode mode) {
      Triangles result = context.<Triangles>createBuffer();
      context.bindBuffer(ARRAY_BUFFER, result);
      context.bufferData(ARRAY_BUFFER, this, STATIC_DRAW);
      result.initVertexCount(getLength() / 3);
      result.initDrawMode(mode);
      return result;
    }

  }

  /**
   * Creates a new all-zero triangle strip with room for the given
   * number of triangles.
   */
  public static native Builder builder(int count) /*-{
    return new Float32Array(3 + 3 * count);
  }-*/;

}
