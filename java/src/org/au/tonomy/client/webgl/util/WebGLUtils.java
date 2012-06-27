package org.au.tonomy.client.webgl.util;

import org.au.tonomy.client.webgl.RenderingContext;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.dom.client.Element;

/**
 * Wrappers for utility methods from webgl-utils.js.
 */
public class WebGLUtils implements IWebGLUtils {

  private static final WebGLUtils INSTANCE = new WebGLUtils();

  /**
   * Returns the singleton WebGLUtils instance.
   */
  public static IWebGLUtils get() {
    return INSTANCE;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void requestAnimFrame(Canvas canvas, IRenderingFunction animator) {
    requestAnimFrame(canvas.getElement(), animator);
  }

  // Native implementation.
  private native void requestAnimFrame(Element canvas,
      IRenderingFunction animator) /*-{
    function callback() {
      animator.@org.au.tonomy.client.webgl.util.IRenderingFunction::tick()();
      var keepGoing = animator.@org.au.tonomy.client.webgl.util.IRenderingFunction::shouldContinue()();
      if (keepGoing)
        $wnd.requestAnimFrame(callback, canvas);
    }
    $wnd.requestAnimFrame(callback, canvas);
  }-*/;

  @Override
  public RenderingContext create3DContext(Canvas canvas) {
    return create3DContext(canvas.getElement());
  }

  // Native implementation
  private native RenderingContext create3DContext(Element canvas) /*-{
    return $wnd.WebGLUtils.create3DContext(canvas);
  }-*/;

}
