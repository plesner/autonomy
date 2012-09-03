package org.au.tonomy.client.codemirror;

import org.au.tonomy.shared.util.IThunk;

/**
 * Container class for the method that builds the autonomy key map.
 */
public class AutonomyKeyMap {

  /**
   * Returns the default autonomy key mapping.
   */
  public static KeyMap get() {
    return KeyMap
        .newBuilder("autonomy")
        .addBinding("Cmd-S", getForwarder(IAction.Type.SAVE))
        .addBinding("Cmd-Z", getForwarder(IAction.Type.UNDO))
        .addFallthrough("basic")
        .build();
  }

  /**
   * Returns a listener that will forward the event to the code mirror
   * given as an argument.
   */
  private static IThunk<CodeMirror> getForwarder(final IAction.Type type) {
    return new IThunk<CodeMirror>() {
      @Override
      public void call(CodeMirror value) {
        IThunk<IAction.Type> listener = value.getActionListener();
        if (listener != null)
          listener.call(type);
      }
    };
  }

}
