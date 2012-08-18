package org.au.tonomy.client.widget.workspace;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
/**
 * The header at the top of the workspace.
 */
public class HeaderWidget extends Composite {

  private static IHeaderWidgetUiBinder BINDER = GWT.create(IHeaderWidgetUiBinder.class);
  interface IHeaderWidgetUiBinder extends UiBinder<Widget, HeaderWidget> { }

  public HeaderWidget() {
    initWidget(BINDER.createAndBindUi(this));
  }

}
