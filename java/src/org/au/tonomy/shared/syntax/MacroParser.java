package org.au.tonomy.shared.syntax;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.au.tonomy.shared.syntax.Ast.MacroCall;
import org.au.tonomy.shared.util.Assert;



/**
 * Utility for parsing keyword expressions.
 */
public class MacroParser {

  /**
   * A single component in a keyword expression sequence.
   */
  public static abstract class Component {

    /**
     * Returns this component's keyword string if it is a keyword.
     */
    public String asKeyword() {
      return null;
    }

    /**
     * Returns the type of this placeholder, or null if this isn't a
     * placeholder.
     */
    public Placeholder.Type getPlaceholderType() {
      return null;
    }

    /**
     * Is this a placeholder?
     */
    public boolean isPlaceholder() {
      return false;
    }

  }

  /**
   * A keyword component.
   */
  public static class Keyword extends Component {

    private final String word;

    public Keyword(String word) {
      this.word = word;
    }

    @Override
    public String asKeyword() {
      return word;
    }

  }

  /**
   * A placeholder component that corresponds to a subexpression.
   */
  public static class Placeholder extends Component {

    public enum Type {

      LAZY_EXPRESSION(true, true),
      EAGER_EXPRESSION(false, true),
      LAZY_STATEMENT(true, false),
      EAGER_STATEMENT(false, false);

      private final boolean isLazy;
      private final boolean suppressSemi;

      private Type(boolean isLazy, boolean suppressSemi) {
        this.isLazy = isLazy;
        this.suppressSemi = suppressSemi;
      }

      /**
       * Is this placeholder required to be followed by a semicolon
       * when the macro is used as a statement?
       */
      public boolean suppressSemi() {
        return suppressSemi;
      }

    }

    private final Type type;

    public Placeholder(Type type) {
      this.type = type;
    }

    @Override
    public Type getPlaceholderType() {
      return type;
    }

    @Override
    public boolean isPlaceholder() {
      return true;
    }

  }

  /**
   * A state in the keyword parsing state machine.
   */
  private static class StateInfo {

    private final Map<String, StateInfo> onWord = new HashMap<String, StateInfo>();
    private StateInfo onAst = null;
    private Macro onEnd = null;
    private boolean suppressSemi = false;

    /**
     * Add states corresponding to the given components, starting from
     * index, to this state.
     */
    public void addStatement(Macro statement, int index) {
      List<Component> components = statement.getComponents();
      if (index == components.size()) {
        // We're at the end of a sequence so this is an end state.
        Assert.that(onEnd == null);
        onEnd = statement;
      } else {
        Component next = components.get(index);
        String keyword = next.asKeyword();
        if (keyword == null) {
          // This component is a placeholder. Add states that follow
          // when parsing an ast.
          if (onAst == null) {
            onAst = new StateInfo();
            suppressSemi = next.getPlaceholderType().suppressSemi;
          }
          onAst.addStatement(statement, index + 1);
          Assert.that(suppressSemi == next.getPlaceholderType().suppressSemi);
        } else {
          // This component is a keyword, add a state that follows if
          // we see that keyword.
          if (!onWord.containsKey(keyword))
            onWord.put(keyword, new StateInfo());
          onWord.get(keyword).addStatement(statement, index + 1);
        }
      }
    }

    public boolean suppressSemi() {
      return suppressSemi;
    }

  }

  /**
   * A state while parsing a keyword sequence.
   */
  public static class State {

    private final State prev;
    private final Ast value;
    private final StateInfo info;

    public State(State prev, Ast value, StateInfo info) {
      this.prev = prev;
      this.value = value;
      this.info = info;
    }

    /**
     * Returns the next state, given that we're at the specified word,
     * or null if no next state will work.
     */
    public State advance(String word) {
      StateInfo nextInfo = info.onWord.get(word);
      return nextInfo == null ? null : new State(this, null, nextInfo);
    }

    /**
     * Returns the next state, given that we've just parsed the given
     * subexpression.
     */
    public State advance(Ast value) {
      return new State(this, value, info.onAst);
    }

    /**
     * Returns true if this is a legal final state.
     */
    public boolean isFinal() {
      return info.onEnd != null;
    }

    /**
     * Returns the macro completed by this state.
     */
    public Macro getMacro() {
      return info.onEnd;
    }

    /**
     * Returns true if the subexpression corresponding to this state
     * should not be forced to end with a semicolon.
     */
    public boolean suppressSemi() {
      return info.suppressSemi();
    }

    /**
     * If this is a final state, returns the syntax tree describing
     * the correctly parsed input.
     */
    public Ast build() {
      Macro macro = info.onEnd;
      List<Component> components = macro.getComponents();
      List<Ast> args = new ArrayList<Ast>();
      addArguments(args, components, components.size() - 1);
      return new MacroCall(macro, args);
    }

    private void addArguments(List<Ast> args, List<Component> components,
        int index) {
      if (prev != null)
        prev.addArguments(args, components, index - 1);
      if (value != null) {
        Component component = components.get(index);
        Ast arg;
        if (component.getPlaceholderType().isLazy) {
          arg = new Ast.Lambda(Collections.<String>emptyList(), value);
        } else {
          arg = value;
        }
        args.add(arg);
      }
    }


    /**
     * Does this state allow a subexpression?
     */
    public boolean isPlaceholder() {
      return info.onAst != null;
    }

  }

  private final List<Macro> macros = new ArrayList<Macro>();
  private final StateInfo root;

  public MacroParser() {
    this.root = new StateInfo();
  }

  public MacroParser(MacroParser parent) {
    this();
    for (Macro macro : parent.macros)
      addSequence(macro);
  }

  public void addSequence(Macro statement) {
    macros.add(statement);
    root.addStatement(statement, 0);
  }

  public State getInitialState() {
    return new State(null, null, root);
  }

}
