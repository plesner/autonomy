package org.au.tonomy.client.webgl;

import static org.au.tonomy.client.webgl.RenderingContext.ARRAY_BUFFER;
import static org.au.tonomy.client.webgl.RenderingContext.STATIC_DRAW;

/**
 * A buffer that contains data for a triangle strip.
 */
public class TriangleStrip extends Buffer {

  protected TriangleStrip() { }

  /**
   * Initializes the vertex count field.
   */
  private final native void initVertexCount(int count) /*-{
    this.vertexCount = count;
  }-*/;

  /**
   * Returns the number of vertices stored in this strip.
   */
  public final native int getVertexCount() /*-{
    return this.vertexCount;
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
    public final native Builder set(int index, double x0, double y0, double z0,
        double x1, double y1, double z1) /*-{
      var offset = 3 + (6 * index);
      this[offset + 0] = x0;
      this[offset + 1] = y0;
      this[offset + 2] = z0;
      this[offset + 3] = x1;
      this[offset + 4] = y1;
      this[offset + 5] = z1;
      return this;
    }-*/;

    /**
     * Builds a triangle strip containing the data from this builder.
     */
    public final TriangleStrip build(RenderingContext context) {
      TriangleStrip result = context.<TriangleStrip>createBuffer();
      context.bindBuffer(ARRAY_BUFFER, result);
      context.bufferData(ARRAY_BUFFER, this, STATIC_DRAW);
      result.initVertexCount(getLength() / 3);
      return result;
    }

  }

  /**
   * Creates a new all-zero triangle strip with room for the given
   * number of triangles.
   */
  public static native Builder builder(int count) /*-{
    return new Float32Array(3 + 6 * count);
  }-*/;

}
