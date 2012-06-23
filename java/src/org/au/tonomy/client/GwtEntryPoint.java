package org.au.tonomy.client;

import org.au.tonomy.client.world.WorldWidget;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;

public class GwtEntryPoint implements EntryPoint {

  @Override
  public void onModuleLoad() {
    WorldWidget widget = new WorldWidget();
    RootPanel.get().add(widget);
    widget.start();
  }

}
