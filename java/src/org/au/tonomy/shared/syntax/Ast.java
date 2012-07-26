package org.au.tonomy.shared.syntax;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.au.tonomy.shared.runtime.AbstractValue;
import org.au.tonomy.shared.runtime.HereValue;
import org.au.tonomy.shared.runtime.IScope;
import org.au.tonomy.shared.runtime.IValue;
import org.au.tonomy.shared.runtime.LambdaValue;
import org.au.tonomy.shared.runtime.MethodRegister;
import org.au.tonomy.shared.runtime.ModuleValue;
import org.au.tonomy.shared.runtime.NullValue;
import org.au.tonomy.shared.runtime.TupleValue;
import org.au.tonomy.shared.syntax.MacroParser.Component;
import org.au.tonomy.shared.util.Assert;

/**
 * A syntax tree node.
 */
public abstract class Ast implements AstOrArguments {

  /**
   * Executes this syntax in the given scope.
   */
  public abstract IValue run(ModuleValue module, IScope scope);

  /**
   * Converts this syntax tree to an ast node.
   */
  public AstNode toAstNode() {
    return AstNode.text(Token.identifier("foo"));
  }

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

    private final Object name;

    public Identifier(Object name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return name.toString();
    }

    @Override
    public IValue run(ModuleValue module, IScope scope) {
      return scope.getValue(name, module);
    }

  }

  public static class Literal extends Ast {

    private final IValue value;

    public Literal(IValue value) {
      this.value = value;
    }

    @Override
    public IValue run(ModuleValue module, IScope scope) {
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
    public IValue run(ModuleValue module, IScope scope) {
      IValue result = null;
      for (int i = 0; i < children.size(); i++)
        result = children.get(i).run(module, scope);
      return result;
    }

    @Override
    public String toString() {
      return "(;" + toString(children) + ")";
    }

  }

  /**
   * The 'here' expression.
   */
  public static class Here extends Ast {

    public Here() { }

    @Override
    public IValue run(ModuleValue module, IScope scope) {
      return new HereValue(module, scope);
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
    public IValue run(ModuleValue module, IScope scope) {
      IValue recvValue = receiver.run(module, scope);
      IValue[] argValues = new IValue[args.size()];
      for (int i = 0; i < args.size(); i++)
        argValues[i] = args.get(i).run(module, scope);
      return recvValue.invoke(op, argValues);
    }

    public List<Ast> getArguments() {
      return args;
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
  public static class MacroCall extends Call {

    private final Macro macro;

    public MacroCall(Macro macro, List<Ast> args) {
      super(new Identifier(macro.getId()), "()", args);
      this.macro = macro;
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
      result.append(toString(getArguments())).append(")");
      return result.toString();
    }

  }

  public static class Tuple extends Ast {

    private final List<Ast> asts;

    public Tuple(List<Ast> asts) {
      this.asts = asts;
    }

    @Override
    public IValue run(ModuleValue module, IScope scope) {
      IValue[] values = new IValue[asts.size()];
      for (int i = 0; i < asts.size(); i++)
        values[i] = asts.get(i).run(module, scope);
      return new TupleValue(values);
    }

  }

  public static class Arguments implements AstOrArguments {

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
    public IValue run(ModuleValue module, IScope scope) {
      return new LambdaValue(params, body, scope, module);
    }

  }

  /**
   * A generic definition.
   */
  public static abstract class Definition extends Ast {

    private final List<Ast> annots;
    private final Object name;
    private final Ast value;
    private final Ast body;

    public Definition(List<Ast> annots, Object name, Ast value, Ast body) {
      this.annots = annots;
      this.name = name;
      this.value = value;
      this.body = body;
    }

    protected List<Ast> getAnnotations() {
      return this.annots;
    }

    protected Object getName() {
      return this.name;
    }

    protected Ast getValue() {
      return this.value;
    }

    protected Ast getBody() {
      return this.body;
    }

  }

  /**
   * A locally scoped definition.
   */
  public static class LocalDefinition extends Definition {

    public LocalDefinition(List<Ast> annots, Object name, Ast value, Ast body) {
      super(annots, name, value, body);
    }

    @Override
    public IValue run(ModuleValue outerContext, final IScope outerScope) {
      final List<IValue> annotValues;
      final IValue v = getValue().run(outerContext, outerScope);
      if (getAnnotations().isEmpty()) {
        annotValues = Collections.emptyList();
      } else {
        annotValues = new ArrayList<IValue>();
        for (Ast annotAst : getAnnotations()) {
          IValue annotValue = annotAst.run(outerContext, outerScope);
          annotValues.add(annotValue);
        }
      }
      return getBody().run(outerContext, new IScope() {
        @Override
        public IValue getValue(Object name, ModuleValue innerContext) {
          return getName().equals(name)
              ? v
              : outerScope.getValue(name, innerContext);
        }
      });
    }

  }

  /**
   * A toplevel definition.
   */
  public static class ToplevelDefinition extends Definition {

    public ToplevelDefinition(List<Ast> annots, Object name, Ast value, Ast body) {
      super(annots, name, value, body);
    }

    @Override
    public IValue run(ModuleValue outerContext, final IScope outerScope) {
      final List<IValue> annotValues;
      IValue value = getValue().run(outerContext, outerScope);
      if (getAnnotations().isEmpty()) {
        annotValues = Collections.emptyList();
      } else {
        DeclarationValue declValue = new DeclarationValue(value);
        annotValues = new ArrayList<IValue>();
        for (Ast annotAst : getAnnotations()) {
          IValue annotValue = annotAst.run(outerContext, outerScope);
          annotValue.invoke("()", new IValue[] {declValue});
          annotValues.add(annotValue);
        }
        value = declValue.value;
      }
      outerContext.bind(getName(), value);
      return getBody().run(outerContext, outerScope);
    }

  }

  private static class DeclarationValue extends AbstractValue {

    private static final MethodRegister<DeclarationValue> METHODS = new MethodRegister<DeclarationValue>() {{
      addMethod(".value", new IMethod<DeclarationValue>() {
        @Override
        public IValue invoke(DeclarationValue self, IValue[] args) {
          return self.value;
        }
      });
      addMethod(".set_value", new IMethod<DeclarationValue>() {
        @Override
        public IValue invoke(DeclarationValue self, IValue[] args) {
          return self.value = args[0];
        }
      });
    }};

    private IValue value;

    public DeclarationValue(IValue value) {
      this.value = value;
    }

    @Override
    public IValue invoke(String name, IValue[] args) {
      return METHODS.invoke(name, this, args);
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
