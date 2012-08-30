package org.au.tonomy.client.browser;

import static org.au.tonomy.client.browser.RenderingContext.ARRAY_BUFFER;
import static org.au.tonomy.client.browser.RenderingContext.STATIC_DRAW;

import org.au.tonomy.client.browser.RenderingContext.DrawMode;

/**
 * A buffer that contains data for a triangle strip.
 */
public class TriangleBuffer extends Buffer {

  protected TriangleBuffer() { }

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
  public static class TriangleArray extends Float32Array {

    protected TriangleArray() { }

    /**
     * Sets the coordinates of the start point.
     */
    public final native TriangleArray start(double x, double y, double z) /*-{
      this[0] = x;
      this[1] = y;
      this[2] = z;
      return this;
    }-*/;

    /**
     * Sets the coordinates of the index'th triangle.
     */
    public final native TriangleArray add(int index, double x, double y, double z) /*-{
      var offset = 3 + (3 * index);
      this[offset + 0] = x;
      this[offset + 1] = y;
      this[offset + 2] = z;
      return this;
    }-*/;

    /**
     * Builds a triangle strip containing the data from this builder.
     */
    public final TriangleBuffer toBuffer(RenderingContext context, DrawMode mode) {
      TriangleBuffer result = context.<TriangleBuffer>createBuffer();
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
  public static native TriangleArray builder(int count) /*-{
    return new Float32Array(3 + 3 * count);
  }-*/;

}
