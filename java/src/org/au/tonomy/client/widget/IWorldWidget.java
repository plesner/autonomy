package org.au.tonomy.client.widget;

import org.au.tonomy.shared.util.IMatrix;
import org.au.tonomy.shared.util.IVector;

public interface IWorldWidget<V4 extends IVector, M4 extends IMatrix<V4>> {

  /**
   * Listener interface for getting events from the world widget.
   */
  public interface IListener {
    public void onMouseDown(int x, int y);
    public void onMouseUp();
    public void onMouseMove(int x, int y);
    public void onMouseWheel(int deltaY);
    public void onBeforeResize(int width, int height);
  }

  /**
   * Returns the camera being used by this widget to render the world.
   */
  public ICamera<V4, M4> getCamera();

  /**
   * Attaches an event listener to this widget. There can only be
   * one listener attached.
   */
  public void attachListener(IListener listener);

  /**
   * Ensures that the given thunk is called at the relevant frame rate
   * when the widget is being displayed.
   */
  public void callAtFrameRate(Runnable thunk);

  /**
   * Cancels a previously scheduled callAtFrameRate.
   */
  public void stopCallingAtFrameRate();

  /**
   * Repaints the world.
   */
  public void refresh();

  public void showDragCursor();

  public void hideDragCursor();

}
