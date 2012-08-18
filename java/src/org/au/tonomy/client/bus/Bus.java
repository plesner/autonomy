package org.au.tonomy.client.bus;

import java.util.Collection;

import org.au.tonomy.shared.util.Assert;
import org.au.tonomy.shared.util.IThunk;
import org.au.tonomy.shared.util.IUndo;

/**
 * The central message bus that holds global state.
 */
public abstract class Bus {

  private static Bus instance;

  /**
   * Returns the current status.
   */
  public abstract String getStatus();

  /**
   * Sets the current status;
   */
  public abstract void setStatus(String value);

  /**
   * Adds a callback to invoke when the status changes.
   */
  public abstract IUndo addStatusListener(IThunk<String> listener);

  /**
   * Returns the current set of active messages.
   */
  public abstract Collection<Message> getMessages();

  /**
   * Adds a new message to the set maintained by this bus.
   */
  public abstract void addMessage(Message message);

  /**
   * Adds a callback to invoke when new messages are added.
   */
  public abstract IUndo addMessageAddedListener(IThunk<Message> listener);

  /**
   * Returns the singleton bus instance.
   */
  public static Bus get() {
    return Assert.notNull(instance);
  }

  /**
   * Sets the singleton bus instance to use during this session.
   * @param bus
   */
  public static void set(Bus bus) {
    Assert.isNull(instance);
    instance = Assert.notNull(bus);
  }

}
