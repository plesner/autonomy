package org.au.tonomy.client.bus;

import org.au.tonomy.shared.util.Assert;
import org.au.tonomy.shared.util.IUndo;
import org.au.tonomy.shared.util.UndoList;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;

/**
 * A message about the state of the system.
 */
public class Message {

  /**
   * A listener that is notified about changes to a message object.
   */
  public interface IListener {

    /**
     * Some property of the message has changed.
     */
    public void onChanged(Message message);

    /**
     * The message has been deleted.
     */
    public void onDeleted(Message message);

  }

  private String text;
  private final UndoList<IListener> listeners = UndoList.create();
  private double weight;
  private IUndo addUndo;
  private long expirationStart = 0;
  private int expirationMs = 0;

  public Message(String text) {
    this.text = text;
    this.weight = 1.0;
  }

  /**
   * Returns the text of this message.
   */
  public String getText() {
    return this.text;
  }

  /**
   * Returns the weight of the message. If a message is set to expire
   * its weight will drop towards 0 and when it reaches 0 be deleted.
   */
  public double getWeight() {
    return this.weight;
  }

  /**
   * Instructs this message to fade away over the given timeout and
   * ultimately disappear.
   */
  public void setExpiration(int timeoutMs) {
    expirationStart = System.currentTimeMillis();
    expirationMs = timeoutMs;
    Scheduler.get().scheduleFixedPeriod(new RepeatingCommand() {
      @Override
      public boolean execute() {
        long now = System.currentTimeMillis();
        long spent = now - expirationStart;
        if (spent >= expirationMs) {
          delete();
          return false;
        } else {
          setWeight(1 - (((double) spent) / expirationMs));
          return true;
        }
      }
    }, 1000 / 60);
  }

  public Message setText(String value) {
    this.text = Assert.notNull(value);
    for (IListener listener : listeners)
      listener.onChanged(this);
    return this;
  }

  private void setWeight(double value) {
    Assert.that(value >= 0);
    Assert.that(value <= 1);
    this.weight = value;
    for (IListener listener : listeners)
      listener.onChanged(this);
  }

  private void delete() {
    if (addUndo != null)
      addUndo.undo();
    for (IListener listener : listeners)
      listener.onDeleted(this);
  }


  public IUndo addListener(IListener listener) {
    return listeners.add(listener);
  }

  public void setAddUndo(IUndo undo) {
    this.addUndo = undo;
  }

}
