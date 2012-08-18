package org.au.tonomy.client.widget.workspace;

import org.au.tonomy.client.Bus;
import org.au.tonomy.shared.util.IThunk;
import org.au.tonomy.shared.util.IUndo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
/**
 * The header at the top of the workspace.
 */
public class HeaderWidget extends Composite {

  private static IHeaderWidgetUiBinder BINDER = GWT.create(IHeaderWidgetUiBinder.class);
  interface IHeaderWidgetUiBinder extends UiBinder<Widget, HeaderWidget> { }

  @UiField Label status;
  private IUndo undoStatusListener;

  public HeaderWidget() {
    initWidget(BINDER.createAndBindUi(this));
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    Bus bus = Bus.get();
    undoStatusListener = bus.addStatusListener(new IThunk<String>() {
      @Override
      public void call(String value) {
        status.setText(value);
      }
    });
    status.setText(bus.getStatus());
  }

  @Override
  protected void onUnload() {
    if (undoStatusListener != null) {
      undoStatusListener.undo();
      undoStatusListener = null;
    }
    super.onUnload();
  }

}
