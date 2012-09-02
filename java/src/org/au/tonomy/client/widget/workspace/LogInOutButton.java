package org.au.tonomy.client.widget.workspace;

import com.google.gwt.user.client.ui.Widget;

public class LogInOutButton extends HeaderButton {

  @Override
  protected void initWidget(Widget widget) {
    super.initWidget(widget);
    getLabel().setText("Log in");
  }

}
