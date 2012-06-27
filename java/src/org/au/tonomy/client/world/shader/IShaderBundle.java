package org.au.tonomy.client.world.shader;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

public interface IShaderBundle extends ClientBundle {

  @Source("fragment.glsl")
  public TextResource getFragmentShader();

  @Source("vertex.glsl")
  public TextResource getVertexShader();

}
