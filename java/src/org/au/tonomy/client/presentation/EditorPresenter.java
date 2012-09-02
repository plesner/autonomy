package org.au.tonomy.client.presentation;

import org.au.tonomy.client.Console;
import org.au.tonomy.client.codemirror.IAction;
import org.au.tonomy.client.presentation.IEditorWidget.IChangeEvent;
import org.au.tonomy.client.presentation.IEditorWidget.IPosition;
import org.au.tonomy.shared.ot.IMutableDocument;
import org.au.tonomy.shared.ot.Transform;
import org.au.tonomy.shared.ot.TransformBuilder;
import org.au.tonomy.shared.source.SourceCoordinateMapper;
import org.au.tonomy.shared.util.Assert;
import org.au.tonomy.shared.util.IUndo;
import org.au.tonomy.shared.util.UndoList;

public class EditorPresenter {

  /**
   * Interface for high-level editor events.
   */
  public interface IListener {

    /**
     * Called when the contents of the editor changes.
     */
    public void onChange(Transform transform);

  }

  private final SourceCoordinateMapper mapper = new SourceCoordinateMapper();
  private final IEditorWidget widget;
  private final UndoList<IListener> listeners = UndoList.create();
  private IMutableDocument doc;

  public EditorPresenter(IEditorWidget widget) {
    this.widget = widget;
    widget.addListener(widgetListener);
  }

  /**
   * Sets the text of this editor.
   */
  public void setContents(IMutableDocument doc) {
    this.doc = doc;
    this.widget.setContents(doc);
    this.mapper.resetSource(doc.getText());
  }

  public String getContents() {
    return this.mapper.getSource();
  }

  /**
   * Add an editor event listener.
   */
  public IUndo addListener(IListener listener) {
    return listeners.add(listener);
  }

  private void onContentsChanged(IChangeEvent event) {
    // Map the event (row, col) to offsets.
    IPosition fromPos = event.getFrom();
    int fromOffset = mapper.getOffset(fromPos.getLine(), fromPos.getChar());
    IPosition toPos = event.getTo();
    int toOffset = mapper.getOffset(toPos.getLine(), toPos.getChar());
    // Create a transformation that corresponds to the change.
    TransformBuilder builder = new TransformBuilder();
    builder.skip(fromOffset);
    if (fromOffset != toOffset)
      builder.delete(mapper.substring(fromOffset, toOffset));
    for (int i = 0; i < event.getTextLineCount(); i++) {
      if (i > 0)
        builder.insert("\n");
      builder.insert(event.getTextLine(i));
    }
    builder.skip(mapper.getLength() - toOffset);
    Transform transform = builder.flush();
    Assert.equals(mapper.getLength(), transform.getInputLength());
    for (IListener listener : listeners)
      listener.onChange(transform);
    doc.apply(transform);
    mapper.apply(transform);
  }

  private final IEditorWidget.IListener widgetListener = new IEditorWidget.IListener() {
    @Override
    public void onChanged(IChangeEvent event) {
      onContentsChanged(event);
    }
    public void onAction(IAction.Type action) {
      Console.log(action.name());
    }
  };

}
