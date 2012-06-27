package org.au.tonomy.client.webgl.util;

/**
 * Instances of this interface can control display refreshing.
 */
public interface IRenderingFunction {

  /**
   * Repaint the display.
   */
  public void tick();

  /**
   * Returns true if this refresher should be called again.
   */
  public boolean shouldContinue();

}