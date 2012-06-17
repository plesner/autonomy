package org.au.tonomy.gwt.client;

import org.au.tonomy.gwt.client.canvas.WorldWidget;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;

public class GwtEntryPoint implements EntryPoint {

  @Override
  public void onModuleLoad() {
    RootPanel.get().add(new WorldWidget());
  }

}
