package org.au.tonomy.client.widget.workspace;

import org.au.tonomy.client.presentation.IEditorWidget;
import org.au.tonomy.client.presentation.IWorldWidget;
import org.au.tonomy.client.webgl.util.Mat4;
import org.au.tonomy.client.webgl.util.Vec4;
import org.au.tonomy.client.widget.EditorWidget;
import org.au.tonomy.client.widget.WorldWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
/**
 * The toplevel workspace.
 */
public class WorkspaceWidget extends Composite {

  private static IWorkspaceWidgetUiBinder BINDER = GWT.create(IWorkspaceWidgetUiBinder.class);
  interface IWorkspaceWidgetUiBinder extends UiBinder<Widget, WorkspaceWidget> { }

  @UiField HeaderWidget header;
  @UiField WorldWidget world;
  @UiField EditorWidget editor;

  public WorkspaceWidget() {
    initWidget(BINDER.createAndBindUi(this));
  }

  public IEditorWidget getEditor() {
    return this.editor;
  }

  public IWorldWidget<Vec4, Mat4> getWorld() {
    return world;
  }

}
