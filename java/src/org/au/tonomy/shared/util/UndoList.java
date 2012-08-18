package org.au.tonomy.shared.util;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
/**
 * A simple utility for maintining a list where adding an element returns
 * an undo operation that removes the element again.
 */
public class UndoList<L> implements Iterable<L> {

  private List<L> elements = Collections.emptyList();

  /**
   * Add an element to the list, returning an undo that removes the
   * element again.
   */
  public IUndo add(final L value) {
    if (elements.isEmpty())
      elements = Factory.newLinkedList();
    elements.add(value);
    return new IUndo() {
      @Override
      public void undo() {
        elements.remove(value);
      }
    };
  }

  @Override
  public Iterator<L> iterator() {
    return elements.iterator();
  }

  /**
   * Returns all the elements in this list.
   */
  public List<L> get() {
    return elements;
  }

  /**
   * Convenience factory function.
   */
  public static <L> UndoList<L> create() {
    return new UndoList<L>();
  }

}
