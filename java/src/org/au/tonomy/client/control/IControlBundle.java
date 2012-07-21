package org.au.tonomy.client.control;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

public interface IControlBundle extends ClientBundle {

  @Source("control.aut")
  public TextResource getControlScript();

}
