package org.au.tonomy.client.presentation;

import org.au.tonomy.shared.ot.IMutableDocument;
import org.au.tonomy.shared.util.IUndo;

/**
 * Abstract interface for editor widgets.
 */
public interface IEditorWidget {

  /**
   * Represents a position within the editor.
   */
  public interface IPosition {

    /**
     * Returns the line of this position.
     */
    public int getLine();

    /**
     * Returns the character position within the line.
     */
    public int getChar();

  }

  /**
   * A change to the contents of the editor.
   */
  public interface IChangeEvent {

    /**
     * Returns the (pre-change) position where the change started.
     */
    public IPosition getFrom();

    /**
     * Returns the (pre-change) position where the change ended.
     */
    public IPosition getTo();

    /**
     * Returns the number of lines replaced at the changed range.
     */
    public int getTextLineCount();

    /**
     * Returns the index'th line replaced at the changed range.
     */
    public String getTextLine(int index);

  }

  /**
   * Interface for listening to editor widget events.
   */
  public interface IListener {

    /**
     * The contents of this widget has changed.
     */
    public void onChanged(IChangeEvent event);

  }

  /**
   * Adds a listener that will be notified of widget changes.
   */
  public IUndo addListener(IListener listener);

  /**
   * Sets the complete contents of this widget.
   */
  public void setContents(IMutableDocument text);

}
