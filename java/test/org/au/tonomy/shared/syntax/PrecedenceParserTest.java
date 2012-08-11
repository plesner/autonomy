package org.au.tonomy.shared.syntax;

import static org.au.tonomy.testing.TestUtils.isOperator;
import junit.framework.TestCase;

import org.au.tonomy.shared.syntax.IOperatorRegistry.Precedence;
import org.au.tonomy.shared.syntax.PrecedenceParser.Fix;
import org.au.tonomy.shared.syntax.PrecedenceParser.IFactory;
import org.au.tonomy.shared.syntax.PrecedenceParser.Position;
import org.junit.Test;
/**
 * Test of the precedence parser.
 */
public class PrecedenceParserTest extends TestCase {

  private static final IFactory<String> FACTORY = new IFactory<String>() {
    @Override
    public String newSuffix(String arg, String op) {
      return "(" + arg + " " + op + ")";
    }
    @Override
    public String newPrefix(String op, String arg) {
      return "(" + op + " " + arg + ")";
    }

    @Override
    public String newInfix(String left, String op, String right) {
      return "(" + left + " " + op + " " + right + ")";
    }
  };

  private static final IOperatorRegistry REGISTRY = new IOperatorRegistry() {
    @Override
    public boolean preferPrefix(String op) {
      return op.contains("p");
    }
    @Override
    public Precedence getPrecedence(String a, String b) {
      char aLast = a.charAt(a.length() - 1);
      int aVal = ('0' <= aLast && aLast <= '9') ? aLast - '0' : 0;
      char bLast = b.charAt(b.length() - 1);
      int bVal = ('0' <= bLast && bLast <= '9') ? bLast - '0' : 0;
      if (aVal < bVal) {
        return Precedence.TIGHTER;
      } else if (aVal == bVal) {
        return Precedence.SAME;
      } else {
        return Precedence.LOOSER;
      }
    }
    @Override
    public Associativity getAssociativity(String op) {
      return op.contains("r") ? Associativity.RIGHT : Associativity.LEFT;
    }
    @Override
    public boolean preferSuffix(String op) {
      return op.contains("s");
    }
  };

  private static Fix getContextFreeType(String op) {
    if (REGISTRY.preferPrefix(op)) {
      return Fix.PREFIX;
    } else if (REGISTRY.preferSuffix(op)) {
      return Fix.SUFFIX;
    } else {
      return Fix.INFIX;
    }
  }

  private String parse(String... inputs) {
    PrecedenceParser<String> parser = new PrecedenceParser<String>(REGISTRY, FACTORY);
    for (String input : inputs) {
      if (isOperator(input)) {
        parser.addOperator(input);
      } else {
        parser.addOperand(input);
      }
    }
    return parser.flush();
  }

  @Test
  public void testPrecedence() {
    // Plain associativity, without precedence.
    assertEquals("(1 + 1)", parse("1", "+", "1"));
    assertEquals("((1 + 1) + 1)", parse("1", "+", "1", "+", "1"));
    assertEquals("(((1 + 1) + 1) + 1)",
        parse("1", "+", "1", "+", "1", "+", "1"));
    assertEquals("(1 +r (1 +r 1))", parse("1", "+r", "1", "+r", "1"));
    assertEquals("(1 +r (1 +r (1 +r 1)))",
        parse("1", "+r", "1", "+r", "1", "+r", "1"));

    // Infix precedence.
    assertEquals("((a *1 b) +2 c)", parse("a", "*1", "b", "+2", "c"));
    assertEquals("(a +2 (b *1 c))", parse("a", "+2", "b", "*1", "c"));
    assertEquals("((a *1 b) +2 (c *1 d))",
        parse("a", "*1", "b", "+2", "c", "*1", "d"));

    // Prefix precedence
    assertEquals("(a + (-p b))", parse("a", "+", "-p", "b"));
    assertEquals("(a -p (-p b))", parse("a", "-p", "-p", "b"));
    assertEquals("(a -p (-p (-p b)))", parse("a", "-p", "-p", "-p", "b"));

    // Suffix precedence.
    assertEquals("((a -s) + b)", parse("a", "-s", "+", "b"));
    assertEquals("((a -s) -s b)", parse("a", "-s", "-s", "b"));
    assertEquals("(((a -s) -s) -s b)", parse("a", "-s", "-s", "-s", "b"));
  }

  /**
   * Returns a string containing the position states after each of the
   * given tokens.
   */
  private String getRawPositions(String... tokens) {
    Position pos = Position.BEFORE_OPERAND;
    StringBuilder buf = new StringBuilder();
    for (String token : tokens) {
      if (isOperator(token)) {
        pos = pos.getNextAfterOperator(getContextFreeType(token));
      } else {
        pos = pos.getNextAfterOperand();
      }
      switch (pos) {
      case AFTER_OPERAND:
        buf.append("a");
        break;
      case BEFORE_OPERAND:
        buf.append("b");
        break;
      case INVALID:
        buf.append("x");
        break;
      }
    }
    return buf.toString();
  }

  public void testContextFreePositions() {
    assertEquals("bbba", getRawPositions("-p", "-p", "-p", "x"));
    assertEquals("bbba", getRawPositions("-", "-", "-", "x"));
    assertEquals("bbba", getRawPositions("-s", "-s", "-s", "x"));
    assertEquals("aaaa", getRawPositions("x", "-s", "-s", "-s"));
    assertEquals("aaaa", getRawPositions("x", "-p", "-p", "-p"));
    assertEquals("abbb", getRawPositions("x", "-", "-", "-"));
    assertEquals("bax", getRawPositions("-p", "a", "a"));
  }

  private String getTypes(String... tokens) {
    Position pos = Position.BEFORE_OPERAND;
    StringBuilder buf = new StringBuilder();
    for (int i = 0; i < tokens.length; i++) {
      String token = tokens[i];
      Fix type;
      if (isOperator(token)) {
        boolean nextIsOperand = (i + 1 < tokens.length) && !isOperator(tokens[i + 1]);
        type = pos.getFix(token, nextIsOperand, REGISTRY);
        switch (type) {
        case INFIX:
          buf.append("i");
          break;
        case PREFIX:
          buf.append("p");
          break;
        case SUFFIX:
          buf.append("s");
          break;
        }
        pos = pos.getNextAfterOperator(type);
      } else {
        pos = pos.getNextAfterOperand();
        buf.append("o");
      }
    }
    return buf.toString();
  }

  /**
   * Tests that the operators are interpreted using the correct fix
   * in different positions. In particular, prefix and suffix operators
   * should be perfectly symmetrical.
   */
  public void testContextFixes() {
    assertEquals("pppo", getTypes("-p", "-p", "-p", "x"));
    assertEquals("oio", getTypes("x", "-", "y"));
    assertEquals("oio", getTypes("x", "-p", "y"));
    assertEquals("oippo", getTypes("x", "-p", "-p", "-p", "y"));
    assertEquals("oio", getTypes("x", "-s", "y"));
    assertEquals("ossio", getTypes("x", "-s", "-s", "-s", "y"));
  }

  @Test
  public void testUtilities() {
    assertEquals(Precedence.LOOSER, REGISTRY.getPrecedence("+2", "*1"));
    assertEquals(Precedence.SAME, REGISTRY.getPrecedence("+1", "*1"));
    assertEquals(Precedence.TIGHTER, REGISTRY.getPrecedence("+1", "*2"));
    assertEquals(Precedence.TIGHTER, REGISTRY.getPrecedence("+", "*1"));
    assertEquals(Precedence.SAME, REGISTRY.getPrecedence("+", "*"));
  }

}
