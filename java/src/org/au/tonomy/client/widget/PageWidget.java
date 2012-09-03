package org.au.tonomy.client.widget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

public class PageWidget extends Composite {

  private static IPageWidgetUiBinder BINDER = GWT.create(IPageWidgetUiBinder.class);
  interface IPageWidgetUiBinder extends UiBinder<Widget, PageWidget> { }

  @UiField FlowPanel contents;

  public PageWidget() {
    initWidget(BINDER.createAndBindUi(this));
  }

  public void setContents(Widget widget) {
    contents.add(widget);
  }

}
