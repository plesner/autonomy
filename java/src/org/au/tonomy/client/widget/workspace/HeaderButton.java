package org.au.tonomy.client.widget.workspace;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
/**
 * A button in the top header.
 */
public class HeaderButton extends Composite {

  private static IHeaderButtonUiBinder BINDER = GWT.create(IHeaderButtonUiBinder.class);
  interface IHeaderButtonUiBinder extends UiBinder<Widget, HeaderButton> { }

  @UiField Label label;

  public HeaderButton() {
    initWidget(BINDER.createAndBindUi(this));
  }

  protected Label getLabel() {
    return label;
  }

}
