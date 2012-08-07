package org.au.tonomy.client.world.shader;

import org.au.tonomy.client.webgl.UniformLocation;
import org.au.tonomy.client.webgl.util.Vec3;

/**
 * Linked program representing the board shader program.
 */
public class BoardProgram extends GenericProgram {

  private UniformLocation u3fvPosition;

  @Override
  protected void bindLocations() {
    super.bindLocations();
    this.u3fvPosition = getUniformLocation("uPosition");
  }

  /**
   * Sets the full locations and progress.
   */
  public void setLocation(Vec3 position) {
    getContext().uniform3fv(u3fvPosition, position);
  }

}
