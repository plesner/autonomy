package org.au.tonomy.client.widget;

import org.au.tonomy.client.codemirror.CodeMirror;
import org.au.tonomy.shared.util.Assert;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class EditorWidget extends Composite {

  private static EditorWidgetUiBinder BINDER = GWT.create(EditorWidgetUiBinder.class);
  interface EditorWidgetUiBinder extends UiBinder<Widget, EditorWidget> { }

  static {
    // CodeMirror.defineMode(new AutonomyMode());
  }

  @UiField DivElement container;
  private CodeMirror.Builder builder;
  private CodeMirror mirror;

  public EditorWidget() {
    this.builder = CodeMirror
        .builder()
        .setLineNumbers(true);
//        .setMode("autonomy");
    initWidget(BINDER.createAndBindUi(this));
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    if (this.builder != null) {
      this.mirror = builder.build(container);
      this.builder = null;
    }
  }

  public void setContents(String value) {
    Assert.notNull(this.mirror).setValue(value);
  }

}
