package org.au.tonomy.client.codemirror;

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
   * A utility for configuring a codemirror instance.
   */
  public static class Builder {

    private final JavaScriptObject config = JavaScriptObject.createObject();

    /**
     * Creates the instance using the given parent element.
     */
    public native CodeMirror build(Element parent) /*-{
      return $wnd.CodeMirror(parent, this.@org.au.tonomy.client.codemirror.CodeMirror.Builder::config);
    }-*/;

    /**
     * Sets the mode to use.
     */
    public native Builder setMode(String mode) /*-{
      this.@org.au.tonomy.client.codemirror.CodeMirror.Builder::config.mode = mode;
      return this;
    }-*/;

    /**
     * Sets whether to show line numbers.
     */
    public native Builder setLineNumbers(boolean value) /*-{
      this.@org.au.tonomy.client.codemirror.CodeMirror.Builder::config.lineNumbers = value;
      return this;
    }-*/;

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
   * Returns the singleton codemirror constructor.
   */
  public static Builder builder() {
    return new Builder();
  }

}
