package org.au.tonomy.shared.syntax;

import java.util.Collection;
import java.util.List;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import org.au.tonomy.shared.syntax.testdata.TestScript;
import org.au.tonomy.shared.util.Exceptions;
/**
 * Helper for tying the autunit tests into the junit framework. There
 * may be a more ideomatic way to accomplish this but junit's wonderfully
 * creative use of reflection makes it so difficult to understand how
 * it's expected to work that I can't tell.
 */
public class AutUnitTest extends TestSuite {

  private AutUnitTest(Collection<TestScript> scripts) {
    this.setName(AutUnitTest.class.getName());
    for (TestScript script : scripts)
      this.addTest(new AutUnitTestCase(script));
  }

  @Override
  public void runTest(Test test, TestResult result) {
    // Override the runTest method, otherwise JUnit is going to try
    // to use its reflection-based nonsense.
    ((AutUnitTestCase) test).runAsJunit(result);
  }

  public static TestSuite suite() {
    return new AutUnitTest(TestScript.getAll());
  }

  /**
   * Wrapper for a single test script.
   */
  private static class AutUnitTestCase extends TestCase {

    private final TestScript script;

    public AutUnitTestCase(TestScript script) {
      super(script.getName());
      this.script = script;
    }

    private void runScript() {
      String source = script.getSource();
      List<Token> tokens = Tokenizer.tokenize(source);
      MacroParser macros = new MacroParser();
      try {
        Ast ast = Parser.parse(macros, tokens);
      } catch (SyntaxError se) {
        Exceptions.propagate(se);
      }
    }

    public void runAsJunit(TestResult result) {
      result.startTest(this);
      try {
        this.runScript();
      } catch (AssertionFailedError afe) {
        result.addFailure(this, afe);
      } catch (RuntimeException re) {
        result.addError(this, re);
      } finally {
        result.endTest(this);
      }
    }

  }

}


