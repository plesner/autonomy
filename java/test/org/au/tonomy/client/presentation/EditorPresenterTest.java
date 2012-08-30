package org.au.tonomy.client.presentation;

import static org.au.tonomy.client.presentation.FakeEditorWidget.change;
import static org.au.tonomy.testing.TestUtils.del;
import static org.au.tonomy.testing.TestUtils.ins;
import static org.au.tonomy.testing.TestUtils.skp;
import static org.au.tonomy.testing.TestUtils.trans;
import junit.framework.TestCase;

import org.au.tonomy.client.presentation.EditorPresenter.IListener;
import org.au.tonomy.shared.ot.Transform;
import org.au.tonomy.shared.util.Assert;
import org.au.tonomy.testing.AbstractEventChecker;
import org.junit.Test;

public class EditorPresenterTest extends TestCase {

  private static class AbstractListener implements IListener {

    @Override
    public void onChange(Transform transform) {
      fail();
    }

  }

  private static class EventChecker extends AbstractEventChecker<IListener> implements IListener {

    @Override
    public void onChange(Transform transform) {
      getNextExpected().onChange(transform);
    }

    @Override
    protected IListener newRecorder() {
      return new Recorder();
    }

    private class Recorder implements IListener {

      @Override
      public void onChange(final Transform eTransform) {
        addExpectation(new AbstractListener() {
          @Override
          public void onChange(Transform transform) {
            assertEquals(eTransform, transform);
          }
        });
      }

    }

  }

  @Test
  public void testChangeEvents() {
    FakeEditorWidget fakeWidget = new FakeEditorWidget();
    EditorPresenter editor = new EditorPresenter(fakeWidget);
    EventChecker checker = new EventChecker();
    editor.addListener(checker);

    editor.setContents(new FakeDocument("foo\nbar\nbaz"));

    checker.getRecorder().onChange(trans(del("f"), skp(10)));
    fakeWidget.fireChangeEvent(change(0, 0, 0, 1, ""));
    assertEquals("oo\nbar\nbaz", editor.getContents());

    checker.getRecorder().onChange(trans(skp(2), ins("z"), skp(8)));
    fakeWidget.fireChangeEvent(change(0, 2, 0, 2, "z"));
    assertEquals("ooz\nbar\nbaz", editor.getContents());

    checker.getRecorder().onChange(trans(skp(5), del("a"), skp(5)));
    fakeWidget.fireChangeEvent(change(1, 1, 1, 2, ""));
    assertEquals("ooz\nbr\nbaz", editor.getContents());

    checker.getRecorder().onChange(trans(skp(5), del("r\nb"), skp(2)));
    fakeWidget.fireChangeEvent(change(1, 1, 2, 1, ""));
    assertEquals("ooz\nbaz", editor.getContents());

    checker.getRecorder().onChange(trans(skp(7), ins("\nquux")));
    fakeWidget.fireChangeEvent(change(1, 3, 1, 3, "", "quux"));
    assertEquals("ooz\nbaz\nquux", editor.getContents());

    checker.getRecorder().onChange(trans(del("ooz\nbaz\nquux")));
    fakeWidget.fireChangeEvent(change(0, 0, 2, 4, ""));
    assertEquals("", editor.getContents());

    checker.getRecorder().onChange(trans(ins("empty")));
    fakeWidget.fireChangeEvent(change(0, 0, 0, 0, "empty"));
    assertEquals("empty", editor.getContents());

    Assert.that(!checker.expectsMoreEvents());
  }

}
