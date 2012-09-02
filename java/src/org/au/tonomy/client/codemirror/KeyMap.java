package org.au.tonomy.client.codemirror;

import org.au.tonomy.shared.util.IThunk;

import com.google.gwt.core.client.JavaScriptObject;
/**
 * A codemirror key map.
 */
public class KeyMap {

  private final String name;
  private final JavaScriptObject mappings;

  public KeyMap(String name, JavaScriptObject mappings) {
    this.name = name;
    this.mappings = mappings;
  }

  /**
   * The name of this key map.
   */
  public String getName() {
    return this.name;
  }

  /**
   * Returns the key mappings.
   */
  public JavaScriptObject getMappings() {
    return this.mappings;
  }

  /**
   * Returns a new key map builder.
   */
  public static Builder newBuilder(String name) {
    return new Builder(name);
  }

  /**
   * A utility for building key maps.
   */
  public static class Builder {

    private final String name;
    private final JavaScriptObject mappings = JavaScriptObject.createObject();

    private Builder(String name) {
      this.name = name;
    }

    /**
     * Adds a key binding to this builder.
     */
    public native Builder addBinding(String key, IThunk<CodeMirror> handler) /*-{
      var mappings = this.@org.au.tonomy.client.codemirror.KeyMap.Builder::mappings;
      mappings[key] = function (mirror) {
        return handler.@org.au.tonomy.shared.util.IThunk::call(Ljava/lang/Object;)(mirror);
      };
      return this;
    }-*/;

    /**
     * Returns the finished key map.
     */
    public KeyMap build() {
      return new KeyMap(name, mappings);
    }

  }

}
