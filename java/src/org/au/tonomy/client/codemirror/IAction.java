package org.au.tonomy.client.codemirror;
/**
 * The abstract type of object that can execute actions.
 */
public interface IAction {

  public enum Type {
    SAVE,
    UNDO
  }

}
