package autonomy.syntax;

import java.util.List;

import autonomy.syntax.MacroParser.Component;
import autonomy.syntax.MacroParser.Placeholder;


/**
 * The definition of a macro.
 */
public class Macro {

  private final List<Component> components;
  private final boolean suppressEndSemi;

  public Macro(List<Component> components) {
    this.components = components;
    Component last = components.get(components.size() - 1);
    Placeholder.Type type = last.getPlaceholderType();
    this.suppressEndSemi = (type != null) && !type.suppressSemi();
  }

  public List<Component> getComponents() {
    return components;
  }

  public boolean suppressEndSemi() {
    return this.suppressEndSemi;
  }

}
