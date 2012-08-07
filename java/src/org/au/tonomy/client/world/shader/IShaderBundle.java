package org.au.tonomy.client.world.shader;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

public interface IShaderBundle extends ClientBundle {

  @Source("fragment.glsl")
  public TextResource getFragmentShader();

  @Source("units.glsl")
  public TextResource getUnitVertexShader();

  @Source("board.glsl")
  public TextResource getBoardVertexShader();

  @Source("common.glsl")
  public TextResource getVertexShaderCommon();

}
