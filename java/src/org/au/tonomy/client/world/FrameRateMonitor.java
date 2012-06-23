package org.au.tonomy.client.world;
/**
 * Utility for monitoring frame rate and load averaged over N frames.
 */
public class FrameRateMonitor {

  private final int frameCount;
  private final long[] renderTimes;
  private final long[] intervals;

  private long sampleCount = 0;
  private long lastStartTime = 0;
  private int cursor = 0;
  private long totalRenderTime = 0;
  private long totalInterval = 0;

  /**
   * Creates a frame rate monitor that averages over the given number
   * of frames.
   */
  public FrameRateMonitor(int frameCount) {
    this.frameCount = frameCount;
    this.renderTimes = new long[frameCount];
    this.intervals = new long[frameCount];
  }

  /**
   * Record a sample.
   */
  public void record(long startTimeMs, long renderTimeMs) {
    totalRenderTime += renderTimeMs - renderTimes[cursor];
    renderTimes[cursor] = renderTimeMs;
    long newInterval = (lastStartTime == 0 ? 0 : startTimeMs - lastStartTime);
    lastStartTime = startTimeMs;
    totalInterval += newInterval - intervals[cursor];
    intervals[cursor] = newInterval;
    cursor = (cursor + 1) % frameCount;
    sampleCount++;
  }

  /**
   * Have we collected enough data to show yet?
   */
  public boolean hasData() {
    return sampleCount >= frameCount;
  }

  /**
   * Average frame rate.
   */
  public double getFps() {
    return 1000 / (totalInterval / ((double) frameCount));
  }

  /**
   * Average rendering load.
   */
  public double getLoad() {
    return ((double) totalRenderTime) / totalInterval;
  }

}
