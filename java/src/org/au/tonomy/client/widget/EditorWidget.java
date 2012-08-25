package org.au.tonomy.client.widget;

import org.au.tonomy.client.Console;
import org.au.tonomy.client.codemirror.AutonomyMode;
import org.au.tonomy.client.codemirror.ChangeEvent;
import org.au.tonomy.client.codemirror.CodeMirror;
import org.au.tonomy.client.codemirror.Position;
import org.au.tonomy.shared.source.SourceCoordinateMapper;
import org.au.tonomy.shared.util.Assert;
import org.au.tonomy.shared.util.IThunk;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;

public class EditorWidget extends Composite {

  private static EditorWidgetUiBinder BINDER = GWT.create(EditorWidgetUiBinder.class);
  interface EditorWidgetUiBinder extends UiBinder<Widget, EditorWidget> { }

  static {
    CodeMirror.defineMode(new AutonomyMode());
  }

  @UiField HTMLPanel container;
  private CodeMirror.Builder builder;
  private CodeMirror mirror;
  private final SourceCoordinateMapper mapper = new SourceCoordinateMapper();

  public EditorWidget() {
    this.builder = CodeMirror
        .builder()
        .setLineNumbers(true)
        .setMode("autonomy")
        .setChangeListener(changeListener);
    initWidget(BINDER.createAndBindUi(this));
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    if (this.builder != null) {
      this.mirror = builder.build(container.getElement());
      this.builder = null;
    }
  }

  public void setContents(String value) {
    Assert.notNull(this.mirror).setValue(value);
    this.mapper.resetSource(value);
  }

  /**
   * Processes an editor changed event.
   */
  private void onChanged(ChangeEvent event) {
    Position fromPos = event.getFrom();
    int fromOffset = mapper.getOffset(fromPos.getLine(), fromPos.getChar());
    Position toPos = event.getTo();
    int toOffset = mapper.getOffset(toPos.getLine(), toPos.getChar());
    Console.log(fromOffset + " " + toOffset);
  }

  /**
   * The singleton change event listener.
   */
  private final IThunk<ChangeEvent> changeListener = new IThunk<ChangeEvent>() {
    @Override
    public void call(ChangeEvent event) {
      onChanged(event);
    }
  };

}
