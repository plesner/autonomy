package org.au.tonomy.shared.syntax;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.au.tonomy.shared.runtime.Context;
import org.au.tonomy.shared.runtime.IScope;
import org.au.tonomy.shared.runtime.IValue;
import org.au.tonomy.shared.runtime.LambdaValue;
import org.au.tonomy.shared.runtime.NullValue;
import org.au.tonomy.shared.runtime.TupleValue;
import org.au.tonomy.shared.syntax.MacroParser.Component;
import org.au.tonomy.shared.syntax.MacroParser.Placeholder;
import org.au.tonomy.shared.util.Assert;



/**
 * A syntax tree node.
 */
public abstract class Ast extends AstOrArguments {

  /**
   * Executes this syntax in the given scope.
   */
  public abstract IValue run(Context context, IScope scope);

  @Override
  public List<Ast> asArguments() {
    return Arrays.asList(this);
  }

  @Override
  public Ast asAst() {
    return this;
  }

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
      return name;
    }

    @Override
    public IValue run(Context context, IScope scope) {
      return scope.getValue(name, context);
    }

  }

  public static class Literal extends Ast {

    private final IValue value;

    public Literal(IValue value) {
      this.value = value;
    }

    @Override
    public IValue run(Context context, IScope scope) {
      return value;
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
        return new Literal(NullValue.get());
      } else if (children.size() == 1) {
        return children.get(0);
      } else {
        return new Block(children);
      }
    }

    @Override
    public IValue run(Context context, IScope scope) {
      IValue result = null;
      for (int i = 0; i < children.size(); i++)
        result = children.get(i).run(context, scope);
      return result;
    }

    @Override
    public String toString() {
      return "(;" + toString(children) + ")";
    }

  }

  public static class Call extends Ast {

    private final Ast receiver;
    private final String op;
    private final List<Ast> args;

    public Call(Ast receiver, String op, List<Ast> args) {
      this.receiver = receiver;
      this.op = op;
      this.args = args;
    }

    @Override
    public IValue run(Context context, IScope scope) {
      IValue recvValue = receiver.run(context, scope);
      IValue[] argValues = new IValue[args.size()];
      for (int i = 0; i < args.size(); i++)
        argValues[i] = args.get(i).run(context, scope);
      return recvValue.invoke(op, argValues);
    }

    /**
     * Call factory used by the precedence parser to build operator
     * expressions.
     */
    public static final PrecedenceParser.IFactory<AstOrArguments> FACTORY =
        new PrecedenceParser.IFactory<AstOrArguments>() {

      @Override
      public AstOrArguments newSuffix(AstOrArguments arg, String op) {
        return new Call(arg.asAst(), op, Collections.<Ast>emptyList());
      }

      @Override
      public AstOrArguments newPrefix(String op, AstOrArguments arg) {
        return new Call(arg.asAst(), op, Collections.<Ast>emptyList());
      }

      @Override
      public AstOrArguments newInfix(AstOrArguments left, String op,
          AstOrArguments right) {
        return new Call(left.asAst(), op, right.asArguments());
      }

    };

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
    public IValue run(Context context, IScope scope) {
      return null;
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

  public static class Tuple extends Ast {

    private final List<Ast> asts;

    public Tuple(List<Ast> asts) {
      this.asts = asts;
    }

    @Override
    public IValue run(Context context, IScope scope) {
      IValue[] values = new IValue[asts.size()];
      for (int i = 0; i < asts.size(); i++)
        values[i] = asts.get(i).run(context, scope);
      return new TupleValue(values);
    }

  }

  public static class Arguments extends AstOrArguments {

    private final List<Ast> children;

    public Arguments(List<Ast> children) {
      this.children = children;
    }

    public static AstOrArguments create(List<Ast> children) {
      if (children.size() == 1) {
        return children.get(0);
      } else {
        return new Arguments(children);
      }
    }

    @Override
    public List<Ast> asArguments() {
      return children;
    }

    @Override
    public Ast asAst() {
      Assert.that(children.size() == 1);
      return children.get(0);
    }

  }

  /**
   * A function closure.
   */
  public static class Lambda extends Ast {

    private final Ast body;
    private final List<String> params;

    public Lambda(List<String> params, Ast body) {
      this.body = body;
      this.params = params;
    }

    @Override
    public IValue run(Context context, IScope scope) {
      return new LambdaValue(params, body, scope, context);
    }

  }

  public static class Definition extends Ast {

    private final String name;
    private final Ast value;
    private final Ast body;

    public Definition(String name, Ast value, Ast body) {
      this.name = name;
      this.value = value;
      this.body = body;
    }

    @Override
    public IValue run(Context outer, final IScope scope) {
      final IValue v = value.run(outer, scope);
      return body.run(outer, new IScope() {
        @Override
        public IValue getValue(String n, Context inner) {
          return name.equals(n) ? v : scope.getValue(n, inner);
        }
      });
    }

  }

  /**
   * Converts a list of objects to a string. Before each element is
   * put a single whitespace.
   */
  static String toString(List<?> objs) {
    StringBuilder result = new StringBuilder();
    for (Object obj : objs) {
      result.append(" ").append(obj);
    }
    return result.toString();
  }

}
