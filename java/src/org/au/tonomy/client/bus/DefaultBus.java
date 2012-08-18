package org.au.tonomy.client.bus;

import java.util.Collection;

import org.au.tonomy.shared.util.IThunk;
import org.au.tonomy.shared.util.IUndo;
import org.au.tonomy.shared.util.UndoList;

/**
 * The default implementation of a message bus.
 */
public class DefaultBus extends Bus {

  private String status;
  private final UndoList<IThunk<String>> statusListeners = UndoList.create();
  private final UndoList<Message> messages = UndoList.create();
  private final UndoList<IThunk<Message>> messageAddedListeners = UndoList.create();

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
  public Collection<Message> getMessages() {
    return messages.get();
  }

  @Override
  public IUndo addStatusListener(final IThunk<String> listener) {
    return statusListeners.add(listener);
  }

  @Override
  public IUndo addMessageAddedListener(final IThunk<Message> listener) {
    return messageAddedListeners.add(listener);
  }

  @Override
  public void addMessage(Message message) {
    IUndo undo = messages.add(message);
    message.setAddUndo(undo);
    for (IThunk<Message> thunk : messageAddedListeners)
      thunk.call(message);
  }

}
