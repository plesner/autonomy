package org.au.tonomy.client.widget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class MainWidget extends Composite {

  private static MainWidgetUiBinder uiBinder = GWT.create(MainWidgetUiBinder.class);
  interface MainWidgetUiBinder extends UiBinder<Widget, MainWidget> { }

  @UiField(provided=true) WorldWidget world;
  @UiField EditorWidget editor;

  public MainWidget(WorldWidget world) {
    this.world = world;
    initWidget(uiBinder.createAndBindUi(this));
  }

}
