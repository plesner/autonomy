package org.au.tonomy.client.widget.workspace;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
/**
 * The toplevel workspace.
 */
public class WorkspaceWidget extends Composite {

  private static IWorkspaceWidgetUiBinder BINDER = GWT.create(IWorkspaceWidgetUiBinder.class);
  interface IWorkspaceWidgetUiBinder extends UiBinder<Widget, WorkspaceWidget> { }

  @UiField HeaderWidget header;
  @UiField FlowPanel workspace;

  public WorkspaceWidget() {
    initWidget(BINDER.createAndBindUi(this));
  }

  public void setBackground(Widget widget) {
    widget.addStyleName(RESOURCES.css().background());
    workspace.add(widget);
  }

  public void addPanel(Widget widget) {
    widget.addStyleName(RESOURCES.css().panel());
    workspace.add(widget);
  }

  public interface Css extends CssResource {

    public String wholePage();

    public String workspace();

    public String background();

    public String panel();

  }

  public interface Resources extends ClientBundle {

    @Source({"../constants.css", "WorkspaceWidget.css"})
    public Css css();

  }

  public static final Resources RESOURCES = GWT.create(Resources.class);
  static { RESOURCES.css().ensureInjected(); }

}
