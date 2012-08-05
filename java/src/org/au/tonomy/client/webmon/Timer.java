package org.au.tonomy.client.webmon;

public class Timer extends Variable<Timer> {

  protected Timer() { }

  /**
   * Records the duration of an event.
   */
  public final native void record(double duration) /*-{
    this.record(duration);
  }-*/;

  /**
   * Creates a new webmon timer.
   */
  public static native Timer create(String name) /*-{
    return new $wnd.webmon.Timer(name);
  }-*/;

}
