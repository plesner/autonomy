package org.au.tonomy.client.webgl.util;

import org.au.tonomy.client.browser.RenderingContext;

import com.google.gwt.canvas.client.Canvas;

/**
 * Abstract interface for the utility methods from webgl-utils.js.
 */
public interface IWebGL {

  /**
   * Calls the rendering function at the request rate of the display
   * but only if the app is visible.
   */
  public void requestAnimFrame(Canvas canvas, IRenderingFunction animator);

  /**
   * Creates a webgl context.
   */
  public RenderingContext create3DContext(Canvas canvas);

  /**
   * Does this browser support webgl?
   */
  public boolean isSupported();

}
