package org.au.tonomy.shared.syntax;

import java.util.Stack;

import org.au.tonomy.shared.syntax.IOperatorRegistry.Associativity;
import org.au.tonomy.shared.syntax.IOperatorRegistry.Precedence;
import org.au.tonomy.shared.util.Assert;
import org.au.tonomy.shared.util.Pair;
/**
 * A specialized parser for parsing arithmetic expressions according
 * to configurable operator rules.
 */
public class PrecedenceParser<T> {

  /**
   * Factory for reducing subexpressions into bigger ones.
   */
  public interface IFactory<T> {

    /**
     * Builds a new suffix expression.
     */
    public T newSuffix(T arg, String op);

    /**
     * Builds a new prefix expression.
     */
    public T newPrefix(String op, T arg);

    /**
     * Builds a new infix expression.
     */
    public T newInfix(T left, String op, T right);

  }

  /**
   * The different types of use an operator can appear in.
   */
  public enum Fix {
    PREFIX,
    INFIX,
    SUFFIX
  }

  /**
   * The current state of this precedence parser: are we before an
   * operand, after it, or is the division inconsistent?
   *
   * The logic is as follows: the following occurrences of operators
   * are valid:
   *
   * <pre>
   *   [operand] [suf 1] [suf 2] ... [infix] [pre 1] [pre 2] ... [operand]
   *             \ -- after operand -- /     \ -- before operand -- /
   * </pre>
   */
  public enum Position {

    /**
     * We are currently before an operand, either after an infix op
     * or at the beginning of the input.
     */
    BEFORE_OPERAND {

      @Override
      public Position getNextAfterOperator(Fix fix) {
        // We only change state when we see an operator.
        return BEFORE_OPERAND;
      }

      @Override
      public Position getNextAfterOperand() {
        return AFTER_OPERAND;
      }

      @Override
      public Fix getFix(String op, boolean lookaheadIsOperand,
          IOperatorRegistry registry) {
        return registry.preferPrefix(op)
            ? Fix.PREFIX
            : Fix.INFIX;
      }

    },

    /**
     * We are currently after an operand and before either an infix
     * operator or the end of the input.
     */
    AFTER_OPERAND {

      @Override
      public Position getNextAfterOperator(Fix fix) {
        // An infix operator switches us back to anticipating an
        // operand.
        return (fix == Fix.INFIX)
            ? BEFORE_OPERAND
            : AFTER_OPERAND;
      }

      @Override
      public Position getNextAfterOperand() {
        return INVALID;
      }

      @Override
      public Fix getFix(String op, boolean lookaheadIsOperand,
          IOperatorRegistry registry) {
        return (!lookaheadIsOperand && registry.preferSuffix(op))
            ? Fix.SUFFIX
            : Fix.INFIX;
      }

    },

    /**
     * We saw a sequence that is somehow invalid.
     */
    INVALID {

      @Override
      public Position getNextAfterOperator(Fix event) {
        return INVALID;
      }

      @Override
      public Position getNextAfterOperand() {
        return INVALID;
      }

      @Override
      public Fix getFix(String op, boolean lookaheadIsOperand,
          IOperatorRegistry registry) {
        return Fix.INFIX;
      }

    };

    /**
     * Returns the next state given that we're now seeing a particular
     * fix of operator.
     */
    public abstract Position getNextAfterOperator(Fix fix);

    /**
     * Returns the next state to use when we've seen an operand.
     */
    public abstract Position getNextAfterOperand();

    /**
     * Returns the appropriate type to use for the given operator at
     * this position.
     */
    public abstract Fix getFix(String op, boolean lookaheadIsOperand,
        IOperatorRegistry registry);

  }

  private Position position = Position.BEFORE_OPERAND;
  private final Stack<T> rands = new Stack<T>();
  private final Stack<Pair<String, Fix>> rators = new Stack<Pair<String, Fix>>();
  private final IOperatorRegistry registry;
  private final IFactory<T> factory;

  private boolean hasLookahead = false;
  private boolean lookaheadIsOperand = false;
  private T lookaheadOperand = null;
  private String lookaheadOperator = null;

  public PrecedenceParser(IOperatorRegistry registry, IFactory<T> factory) {
    this.registry = registry;
    this.factory = factory;
  }

  public T flush() {
    flushLookahead(false);
    while (!rators.isEmpty()) {
      Pair<String, Fix> top = rators.peek();
      switch (top.getSecond()) {
      case PREFIX:
        reducePrefix();
        break;
      case SUFFIX:
        reduceSuffix();
        break;
      case INFIX:
        reduceInfix();
        break;
      }
    }
    Assert.equals(1, rands.size());
    return rands.peek();
  }

  private void flushLookahead(boolean nextIsOperand) {
    if (hasLookahead) {
      if (lookaheadIsOperand) {
        forceAddOperand(lookaheadOperand);
      } else {
        forceAddOperator(lookaheadOperator, nextIsOperand);
      }
      hasLookahead = false;
    }
  }

  public void addOperand(T rand) {
    flushLookahead(true);
    lookaheadIsOperand = true;
    lookaheadOperand = rand;
    hasLookahead = true;
  }

  private void forceAddOperand(T rand) {
    rands.push(lookaheadOperand);
    position = position.getNextAfterOperand();
  }

  public void addOperator(String op) {
    flushLookahead(false);
    lookaheadIsOperand = false;
    lookaheadOperator = op;
    hasLookahead = true;
  }

  public boolean forceAddOperator(String op, boolean nextIsOperand) {
    Fix fix = position.getFix(op, nextIsOperand, registry);
    position = position.getNextAfterOperator(fix);
    if (rators.isEmpty() || fix == Fix.PREFIX) {
      rators.push(Pair.of(op, fix));
      return true;
    } else if (fix == Fix.SUFFIX) {
      return addSuffix(op, fix);
    } else {
      return addInfix(op, fix);
    }
  }

  /**
   * Adds an infix operator.
   */
  private boolean addInfix(String op, Fix fix) {
    Pair<String, Fix> top = rators.peek();
    String topOp = top.getFirst();
    Fix topFix = top.getSecond();
    if (topFix == Fix.PREFIX) {
      switch (getPrecedence(topOp, op)) {
      case TIGHTER:
        rators.push(Pair.of(op, fix));
        return true;
      case LOOSER:
        reducePrefix();
        if (rators.isEmpty()) {
          rators.push(Pair.of(op, fix));
          return true;
        } else {
          return addInfix(op, fix);
        }
      default:
        // SAME or UNRELATED
        rators.push(Pair.of(op, fix));
        return false;
      }
    } else if (topFix == Fix.SUFFIX) {
      reduceSuffix();
      if (rators.isEmpty()) {
        rators.push(Pair.of(op, fix));
        return true;
      } else {
        return addInfix(op, fix);
      }
    } else {
      switch (getPrecedence(topOp, op)) {
      case LOOSER:
        rators.push(Pair.of(op, fix));
        return true;
      case TIGHTER:
        reduceInfix();
        if (rators.isEmpty()) {
          rators.push(Pair.of(op, fix));
          return true;
        } else {
          return addInfix(op, fix);
        }
      case SAME:
        if (registry.getAssociativity(topOp) == Associativity.LEFT) {
          reduceInfix();
          if (rators.isEmpty()) {
            rators.push(Pair.of(op, fix));
            return true;
          } else {
            return addInfix(op, fix);
          }
        } else {
          rators.push(Pair.of(op, fix));
          return true;
        }
      default:
        reduceSuffix();
        if (rators.isEmpty()) {
          rators.push(Pair.of(op, fix));
          return true;
        } else {
          return addInfix(op, fix);
        }
      }
    }
  }

  private Precedence getPrecedence(String a, String b) {
    if (a.equals(b)) {
      return Precedence.SAME;
    } else {
      return registry.getPrecedence(a, b);
    }
  }

  private boolean addSuffix(String op, Fix fix) {
    Pair<String, Fix> top = rators.peek();
    if (top.getSecond() == Fix.SUFFIX) {
      reduceSuffix();
      if (rators.isEmpty()) {
        rators.push(Pair.of(op, fix));
        return true;
      } else {
        return addSuffix(op, fix);
      }
    } else {
      switch (getPrecedence(top.getFirst(), op)) {
      case TIGHTER:
        rators.push(Pair.of(op, fix));
        return true;
      case LOOSER:
        if (top.getSecond() == Fix.PREFIX)
          reducePrefix();
        else
          reduceInfix();
        if (rators.isEmpty()) {
          rators.push(Pair.of(op, fix));
          return true;
        } else {
          return addSuffix(op, fix);
        }
      default:
        rators.push(Pair.of(op, fix));
        return false;
      }
    }
  }

  private void reduceSuffix() {
    Pair<String, Fix> top = rators.pop();
    T rand = rands.pop();
    rands.push(factory.newSuffix(rand, top.getFirst()));
  }

  private void reducePrefix() {
    Pair<String, Fix> top = rators.pop();
    T rand = rands.pop();
    rands.push(factory.newPrefix(top.getFirst(), rand));
  }

  private void reduceInfix() {
    T right = rands.pop();
    Pair<String, Fix> top = rators.pop();
    T left = rands.pop();
    rands.push(factory.newInfix(left, top.getFirst(), right));
  }

}
