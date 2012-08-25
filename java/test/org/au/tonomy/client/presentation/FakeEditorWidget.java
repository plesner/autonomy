package org.au.tonomy.client.presentation;

import org.au.tonomy.shared.util.IUndo;
import org.au.tonomy.shared.util.UndoList;

public class FakeEditorWidget implements IEditorWidget {

  private final UndoList<IListener> listeners = UndoList.create();

  @Override
  public IUndo addListener(IListener listener) {
    return listeners.add(listener);
  }

  @Override
  public void setContents(String text) {
    // ignore
  }

  /**
   * Creates and returns an editor change event with the given parameters.
   */
  public static IChangeEvent change(int fromLine, int fromCh, int toLine, int toCh,
      String... text) {
    return new FakeChangeEvent(new FakePosition(fromLine, fromCh),
        new FakePosition(toLine, toCh), text);
  }

  /**
   * Fires an editor change event.
   */
  public void fireChangeEvent(IChangeEvent event) {
    for (IListener listener : listeners)
      listener.onChanged(event);
  }

  private static class FakePosition implements IPosition {

    private final int line;
    private final int ch;

    public FakePosition(int line, int ch) {
      this.line = line;
      this.ch = ch;
    }

    @Override
    public int getLine() {
      return this.line;
    }

    @Override
    public int getChar() {
      return this.ch;
    }

  }

  private static class FakeChangeEvent implements IChangeEvent {

    private final IPosition from;
    private final IPosition to;
    private final String[] lines;

    public FakeChangeEvent(IPosition from, IPosition to, String[] lines) {
      this.from = from;
      this.to = to;
      this.lines = lines;
    }

    @Override
    public IPosition getFrom() {
      return from;
    }

    @Override
    public IPosition getTo() {
      return to;
    }

    @Override
    public int getTextLineCount() {
      return lines.length;
    }

    public String getTextLine(int index) {
      return lines[index];
    }

  }

}
