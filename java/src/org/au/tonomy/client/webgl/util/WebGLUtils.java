package org.au.tonomy.client.webgl.util;

import org.au.tonomy.client.webgl.RenderingContext;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.dom.client.Element;

/**
 * Wrappers for utility methods from webgl-utils.js.
 */
public class WebGLUtils {

  /**
   * Calls the rendering function at the request rate of the display
   * but only if the app is visible.
   */
  public static void requestAnimFrame(Canvas canvas, RenderingFunction animator) {
    requestAnimFrame(canvas.getElement(), animator);
  }

  // Native implementation.
  private static native void requestAnimFrame(Element canvas,
      RenderingFunction animator) /*-{
    function callback() {
      animator.@org.au.tonomy.client.webgl.util.RenderingFunction::tick()();
      var keepGoing = animator.@org.au.tonomy.client.webgl.util.RenderingFunction::shouldContinue()();
      if (keepGoing)
        $wnd.requestAnimFrame(callback, canvas);
    }
    $wnd.requestAnimFrame(callback, canvas);
  }-*/;

  /**
   * Creates a webgl context.
   */
  public static RenderingContext create3DContext(Canvas canvas) {
    return create3DContext(canvas.getElement());
  }

  // Native implementation
  private static native RenderingContext create3DContext(Element canvas) /*-{
    return $wnd.WebGLUtils.create3DContext(canvas);
  }-*/;

}
