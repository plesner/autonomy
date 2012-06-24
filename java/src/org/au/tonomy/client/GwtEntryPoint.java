package org.au.tonomy.client;

import org.au.tonomy.client.world.WorldWidget;
import org.au.tonomy.shared.world.World;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;

public class GwtEntryPoint implements EntryPoint {

  @Override
  public void onModuleLoad() {
    World world = new World(8, 8);
    WorldWidget widget = new WorldWidget(world);
    RootPanel.get().add(widget);
    widget.start();
  }

}
