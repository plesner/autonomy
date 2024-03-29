package org.au.tonomy.client.widget;

import org.au.tonomy.client.codemirror.AutonomyKeyMap;
import org.au.tonomy.client.codemirror.AutonomyMode;
import org.au.tonomy.client.codemirror.ChangeEvent;
import org.au.tonomy.client.codemirror.CodeMirror;
import org.au.tonomy.client.codemirror.IAction;
import org.au.tonomy.client.codemirror.IAction.Type;
import org.au.tonomy.client.presentation.IEditorWidget;
import org.au.tonomy.shared.ot.IMutableDocument;
import org.au.tonomy.shared.util.Assert;
import org.au.tonomy.shared.util.IThunk;
import org.au.tonomy.shared.util.IUndo;
import org.au.tonomy.shared.util.UndoList;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;

public class EditorWidget extends Composite implements IEditorWidget {

  private static EditorWidgetUiBinder BINDER = GWT.create(EditorWidgetUiBinder.class);
  interface EditorWidgetUiBinder extends UiBinder<Widget, EditorWidget> { }

  static {
    CodeMirror.defineMode(new AutonomyMode());
    CodeMirror.defineKeyMap(AutonomyKeyMap.get());
  }

  @UiField HTMLPanel container;
  private CodeMirror.Builder builder;
  private CodeMirror mirror;
  private final UndoList<IListener> listeners = UndoList.create();
  private boolean muteEvents = false;

  public EditorWidget() {
    this.builder = CodeMirror
        .builder()
        .setLineNumbers(true)
        .setMode("autonomy")
        .setKeyMap("autonomy")
        .setUndoDepth(0)
        .setChangeListener(changeListener)
        .setActionListener(actionListener);
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

  @Override
  public IUndo addListener(IListener listener) {
    return listeners.add(listener);
  }

  @Override
  public void setContents(IMutableDocument value) {
    try {
      muteEvents = true;
      Assert.notNull(this.mirror).setValue(value.getText());
    } finally {
      muteEvents = false;
    }
  }

  /**
   * Processes an editor changed event.
   */
  private void onChanged(ChangeEvent event) {
    for (IListener listener : listeners)
      listener.onChanged(event);
  }

  private void onAction(Type action) {
    for (IListener listener : listeners)
      listener.onAction(action);
  }

  /**
   * The singleton change event listener.
   */
  private final IThunk<ChangeEvent> changeListener = new IThunk<ChangeEvent>() {
    @Override
    public void call(ChangeEvent event) {
      if (!muteEvents)
        onChanged(event);
    }
  };

  private final IThunk<IAction.Type> actionListener = new IThunk<IAction.Type>() {
    public void call(IAction.Type action) {
      onAction(action);
    }
  };

}
