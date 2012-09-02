package org.au.tonomy.client.codemirror;

import org.au.tonomy.client.util.JavaScriptObjects;
import org.au.tonomy.shared.util.IThunk;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
/**
 * Wrapper around the codemirror editor.
 */
public class CodeMirror extends JavaScriptObject {

  protected CodeMirror() { }

  /**
   * Set the editor content.
   */
  public final native void setValue(String value) /*-{
    this.setValue(value);
  }-*/;

  /**
   * Returns the listener that should be notified of editor actions.
   */
  public final IThunk<IAction.Type> getActionListener() {
    return JavaScriptObjects.getProperty(this, "actionListener");
  }

  /**
   * A utility for configuring a codemirror instance.
   */
  public static class Builder {

    private final JavaScriptObject config = JavaScriptObject.createObject();
    private IThunk<IAction.Type> actionListener = null;

    /**
     * Creates the instance using the given parent element.
     */
    public native CodeMirror build(Element parent) /*-{
      var result = $wnd.CodeMirror(parent, this.@org.au.tonomy.client.codemirror.CodeMirror.Builder::config);
      result.actionListener = this.@org.au.tonomy.client.codemirror.CodeMirror.Builder::actionListener;
      return result;
    }-*/;

    /**
     * Sets the mode to use.
     */
    public Builder setMode(String mode) {
      JavaScriptObjects.setProperty(config, "mode", mode);
      return this;
    }

    /**
     * Sets the maximum number of undo levels that the editor stores.
     * Defaults to 40.
     */
    public Builder setUndoDepth(int depth) {
      JavaScriptObjects.setProperty(config, "undoDepth", depth);
      return this;
    }

    /**
     * Sets the name of the key map to use.
     */
    public Builder setKeyMap(String name) {
      JavaScriptObjects.setProperty(config, "keyMap", name);
      return this;
    }

    /**
     * Sets the listener that will be notified on editor actions.
     */
    public Builder setActionListener(IThunk<IAction.Type> listener) {
      this.actionListener = listener;
      return this;
    }

    /**
     * Sets the change listener that will be notified of editor changes.
     */
    public native Builder setChangeListener(IThunk<ChangeEvent> listener) /*-{
      this.@org.au.tonomy.client.codemirror.CodeMirror.Builder::config.onChange = function (editor, event) {
        listener.@org.au.tonomy.shared.util.IThunk::call(Ljava/lang/Object;)(event);
      };
      return this;
    }-*/;

    /**
     * Sets whether to show line numbers.
     */
    public Builder setLineNumbers(boolean value) {
      JavaScriptObjects.setProperty(config, "lineNumbers", value);
      return this;
    }

  }

  /**
   * Registers a language mode.
   */
  public static native void defineMode(IMode<?> mode) /*-{
    $wnd.CodeMirror.defineMode(
        mode.@org.au.tonomy.client.codemirror.IMode::getName()(),
        function (config, parserConfig) {
          return {
            startState: mode.@org.au.tonomy.client.codemirror.IMode::newStartState().bind(mode),
            token: mode.@org.au.tonomy.client.codemirror.IMode::getNextToken(Lorg/au/tonomy/client/codemirror/Stream;Ljava/lang/Object;).bind(mode)
          };
        });
  }-*/;

  /**
   * Registers a key map.
   */
  public static native void defineKeyMap(KeyMap keyMap) /*-{
    var name = keyMap.@org.au.tonomy.client.codemirror.KeyMap::getName()();
    var mapping = keyMap.@org.au.tonomy.client.codemirror.KeyMap::getMappings()();
    $wnd.CodeMirror.keyMap[name] = mapping;
  }-*/;

  /**
   * Returns the singleton codemirror constructor.
   */
  public static Builder builder() {
    return new Builder();
  }

}
