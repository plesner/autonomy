package org.au.tonomy.shared.syntax;

import java.util.Collection;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import org.au.tonomy.shared.runtime.AbstractValue;
import org.au.tonomy.shared.runtime.Context;
import org.au.tonomy.shared.runtime.Executor;
import org.au.tonomy.shared.runtime.IValue;
import org.au.tonomy.shared.runtime.MethodRegister;
import org.au.tonomy.shared.runtime.NullValue;
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

  public static class TestWrapper extends AbstractValue {

    private static final MethodRegister<TestWrapper> METHODS = new MethodRegister<TestWrapper>() {{
      addMethod(".assert_equals", new IMethod<TestWrapper>() {
        @Override
        public IValue invoke(TestWrapper self, IValue[] args) {
          TestCase.assertEquals(args[0], args[1]);
          return NullValue.get();
        }
      });
      addMethod(".assert_true", new IMethod<TestWrapper>() {
        @Override
        public IValue invoke(TestWrapper self, IValue[] args) {
          TestCase.assertTrue(args[0].isTruthy());
          return NullValue.get();
        }
      });
      addMethod(".assert_false", new IMethod<TestWrapper>() {
        @Override
        public IValue invoke(TestWrapper self, IValue[] args) {
          TestCase.assertFalse(args[0].isTruthy());
          return NullValue.get();
        }
      });
      addMethod(".break", new IMethod<TestWrapper>() {
        @Override
        public IValue invoke(TestWrapper self, IValue[] args) {
          return NullValue.get();
        }
      });
    }};

    @Override
    public IValue invoke(String name, IValue[] args) {
      return METHODS.invoke(name, this, args);
    }

  }

  public static final IValue JUNIT_WRAPPER = new TestWrapper();

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
      Executor exec;
      try {
        exec = Compiler.compile(script.getSource());
      } catch (SyntaxError se) {
        throw Exceptions.propagate(se);
      }
      Context context = new Context();
      context.bind("$test", JUNIT_WRAPPER);
      exec.execute(context);
    }

    public void runAsJunit(TestResult result) {
      result.startTest(this);
      try {
        this.runScript();
      } catch (AssertionFailedError afe) {
        result.addFailure(this, afe);
      } catch (Throwable re) {
        result.addError(this, re);
      } finally {
        result.endTest(this);
      }
    }

  }

}
