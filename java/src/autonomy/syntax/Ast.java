package autonomy.syntax;

import java.util.List;

import autonomy.syntax.MacroParser.Component;
import autonomy.syntax.MacroParser.Placeholder;


/**
 * A syntax tree node.
 */
public abstract class Ast {


  /**
   * An identifier reference.
   */
  public static class Identifier extends Ast {

    private final String name;

    public Identifier(String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return "$" + name;
    }

  }

  /**
   * A sequence of expressions.
   */
  public static class Block extends Ast {

    private final List<Ast> children;

    public Block(List<Ast> children) {
      this.children = children;
    }

    public static Ast create(List<Ast> children) {
      if (children.isEmpty()) {
        return null;
      } else if (children.size() == 1) {
        return children.get(0);
      } else {
        return new Block(children);
      }
    }

    @Override
    public String toString() {
      return "(;" + toString(children) + ")";
    }

  }

  /**
   * A macro invocation.
   */
  public static class MacroCall extends Ast {

    /**
     * A single macro call argument.
     */
    public static class Argument {

      private final Ast value;
      private final Placeholder placeholder;

      public Argument(Ast value, Placeholder placeholder) {
        this.value = value;
        this.placeholder = placeholder;
      }

      @Override
      public String toString() {
        return value.toString();
      }

    }

    private final Macro macro;
    private final List<Argument> args;

    public MacroCall(Macro macro, List<Argument> args) {
      this.macro = macro;
      this.args = args;
    }

    @Override
    public String toString() {
      StringBuilder result = new StringBuilder();
      result.append("(");
      boolean first = true;
      for (Component comp : macro.getComponents()) {
        String keyword = comp.asKeyword();
        if (keyword != null) {
          if (first) {
            first = false;
          } else {
            result.append("-");
          }
          result.append(keyword);
        }
      }
      result.append(toString(args)).append(")");
      return result.toString();
    }

  }

  /**
   * Converts a list of objects to a string. Before each element is
   * put a single whitespace.
   */
  private static String toString(List<?> objs) {
    StringBuilder result = new StringBuilder();
    for (Object obj : objs) {
      result.append(" ").append(obj);
    }
    return result.toString();
  }

}
