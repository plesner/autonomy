package org.au.tonomy.client;

import java.util.LinkedList;

import org.au.tonomy.shared.util.Factory;
import org.au.tonomy.shared.util.IThunk;
import org.au.tonomy.shared.util.IUndo;

/**
 * The default implementation of a message bus.
 */
public class DefaultBus extends Bus {

  private String status;
  private final LinkedList<IThunk<String>> statusListeners = Factory.newLinkedList();

  @Override
  public String getStatus() {
    return this.status;
  }

  @Override
  public void setStatus(String value) {
    this.status = value;
    for (IThunk<String> listener : statusListeners)
      listener.call(value);
  }

  @Override
  public IUndo addStatusListener(final IThunk<String> listener) {
    statusListeners.add(listener);
    return new IUndo() {
      @Override
      public void undo() {
        statusListeners.remove(listener);
      }
    };
  }

}
