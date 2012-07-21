package org.au.tonomy.shared.syntax;

import java.util.Collection;
import java.util.List;

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
    for (TestScript script : scripts) {
      List<TestScript.Section> sections = script.getSections();
      if (sections.size() == 1) {
        this.addTest(new AutUnitTestCase(sections.get(0)));
      } else {
        this.addTest(new AutUnitTestSuite(script.getName(), sections));
      }
    }
  }

  @Override
  public void runTest(Test test, TestResult result) {
    // Override the runTest method, otherwise JUnit is going to try
    // to use its reflection-based nonsense.
    ((ICase) test).runAsJunit(result);
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
   * A single runnable test case.
   */
  private static interface ICase {

    public void runAsJunit(TestResult result);

  }

  /**
   * Wrapper for a single test script.
   */
  private static class AutUnitTestCase extends TestCase implements ICase {

    private final TestScript.Section section;

    public AutUnitTestCase(TestScript.Section section) {
      super(section.getName());
      this.section = section;
    }

    private void runScript() {
      Context context = new Context();
      context.bind("$test", JUNIT_WRAPPER);
      context.bind("$null", NullValue.get());
      Executor exec;
      try {
        exec = Compiler.compile(context, section.getSource());
        assertTrue(section.expectSuccess());
      } catch (SyntaxError se) {
        String expected = section.getExpectedOffendingToken();
        if (expected == null) {
          throw Exceptions.propagate(se);
        } else {
          assertEquals(expected, se.getToken().getValue());
          return;
        }
      }
      exec.execute();
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

  private static class AutUnitTestSuite extends TestSuite implements ICase {

    public AutUnitTestSuite(String name, List<TestScript.Section> sections) {
      super(name);
      for (TestScript.Section section : sections)
        addTest(new AutUnitTestCase(section));
    }

    @Override
    public void runAsJunit(TestResult result) {
      run(result);
    }

    @Override
    public void runTest(Test test, TestResult result) {
      ((ICase) test).runAsJunit(result);
    }

  }

}
