package org.au.tonomy.shared.syntax;

import java.util.List;

import org.au.tonomy.shared.syntax.MacroParser.Component;
import org.au.tonomy.shared.syntax.MacroParser.Placeholder;

/**
 * The definition of a macro.
 */
public class Macro {

  private final List<Component> components;
  private final boolean suppressEndSemi;
  private final Ast.Lambda body;
  private final Object id = new Object();

  public Macro(List<Component> components, Ast.Lambda body) {
    this.components = components;
    Component last = components.get(components.size() - 1);
    Placeholder.Type type = last.getPlaceholderType();
    this.suppressEndSemi = (type != null) && !type.suppressSemi();
    this.body = body;
  }

  public Object getId() {
    return id;
  }

  public List<Component> getComponents() {
    return components;
  }

  public Ast.Lambda getBody() {
    return body;
  }

  public boolean suppressEndSemi() {
    return this.suppressEndSemi;
  }

}
