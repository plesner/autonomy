package org.au.tonomy.client.widget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class MessageWidget extends Composite {

  private static IMessageWidgetUiBinder BINDER = GWT.create(IMessageWidgetUiBinder.class);
  interface IMessageWidgetUiBinder extends UiBinder<Widget, MessageWidget> { }

  @UiField FlowPanel message;
  @UiField Label text;

  public MessageWidget() {
    initWidget(BINDER.createAndBindUi(this));
  }

  public void setText(String value) {
    text.setText(value);
  }

  public void setWeight(double weight) {
    Style style = getStyleElement().getStyle();
    style.setOpacity(weight);
  }

}
