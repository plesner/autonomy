package org.au.tonomy.client.widget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class NotSupportedWidget extends Composite {

  private static INotSupportedWidgetUiBinder BINDER = GWT.create(INotSupportedWidgetUiBinder.class);
  interface INotSupportedWidgetUiBinder extends UiBinder<Widget, NotSupportedWidget> { }

  public NotSupportedWidget() {
    initWidget(BINDER.createAndBindUi(this));
  }

}
