package org.au.tonomy.client.widget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class MessagesWidget extends Composite {

  private static IMessagesWidgetUiBinder BINDER = GWT.create(IMessagesWidgetUiBinder.class);
  interface IMessagesWidgetUiBinder extends UiBinder<Widget, MessagesWidget> { }

  public MessagesWidget() {
    initWidget(BINDER.createAndBindUi(this));
  }

}
