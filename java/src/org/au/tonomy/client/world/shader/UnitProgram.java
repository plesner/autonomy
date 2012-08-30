package org.au.tonomy.client.world.shader;

import org.au.tonomy.client.browser.UniformLocation;
import org.au.tonomy.client.webgl.util.Vec3;

/**
 * Linked shader program used to draw units.
 */
public class UnitProgram extends GenericProgram {

  private UniformLocation u3fvSourcePosition;
  private UniformLocation u3fvTargetPosition;
  private UniformLocation u1fProgress;

  @Override
  protected void bindLocations() {
    super.bindLocations();
    this.u3fvSourcePosition = getUniformLocation("uSourcePosition");
    this.u3fvTargetPosition = getUniformLocation("uTargetPosition");
    this.u1fProgress = getUniformLocation("uProgress");
  }

  /**
   * Sets the full locations and progress.
   */
  public void setLocations(Vec3 source, Vec3 target, double progress) {
    getContext().uniform3fv(u3fvSourcePosition, source);
    getContext().uniform3fv(u3fvTargetPosition, target);
    getContext().uniform1f(u1fProgress, progress);
  }

}
