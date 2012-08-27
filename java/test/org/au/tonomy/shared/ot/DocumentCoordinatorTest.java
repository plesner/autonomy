package org.au.tonomy.shared.ot;

import static org.au.tonomy.testing.TestUtils.del;
import static org.au.tonomy.testing.TestUtils.getRandomTransform;
import static org.au.tonomy.testing.TestUtils.ins;
import static org.au.tonomy.testing.TestUtils.skp;
import static org.au.tonomy.testing.TestUtils.trans;

import java.util.List;

import junit.framework.TestCase;

import org.au.tonomy.shared.util.Factory;
import org.au.tonomy.shared.util.Pair;
import org.au.tonomy.testing.ExtraRandom;
import org.junit.Test;

public class DocumentCoordinatorTest extends TestCase {

  private static final IDocument.IProvider PROVIDER =
      PojoDocument.newProvider(Md5Fingerprint.getProvider());

  @Test
  public void testSimpleCoordination() {
    DocumentCoordinator coord = new DocumentCoordinator(PROVIDER,
        "foo bar baz");
    // Initial change.
    IFingerprint f0 = coord.getCurrent().getFingerprint();
    Pair<Transform, IFingerprint> r0 = coord.apply("0", f0,
        trans(skp(3), ins(" quux"), skp(8)));
    assertNull(r0.getFirst());
    assertEquals("foo quux bar baz", coord.getCurrent().getText());
    // Make another change to the initial state and check that it
    // gets applied correctly.
    Pair<Transform, IFingerprint> r1 = coord.apply("0", f0,
        trans(skp(7), ins(" blob"), skp(4)));
    assertEquals(trans(skp(3), ins(" quux"), skp(13)), r1.getFirst());
    assertEquals("foo quux bar blob baz", coord.getCurrent().getText());
    // Make a third change to the initial state and check that the
    // full transform is returned.
    Pair<Transform, IFingerprint> r2 = coord.apply("0", f0,
        trans(skp(3), del(" bar"), skp(4)));
    assertEquals(trans(skp(3), ins(" quux blob"), skp(4)), r2.getFirst());
    assertEquals("foo quux blob baz", coord.getCurrent().getText());
  }

  /**
   * A transformer is a separate thread of editing that posts changes
   * to the coordinator and checks the state it gets back.
   */
  private class Transformer {

    private final String id;
    private final DocumentCoordinator coord;
    private final ExtraRandom random;
    private IDocument current;

    public Transformer(int id, DocumentCoordinator coord, ExtraRandom random) {
      this.id = Integer.toString(id);
      this.coord = coord;
      this.random = random;
      this.current = coord.getCurrent();
    }

    public void fireChange() {
      Transform transform = getRandomTransform(random, current.getText());
      Pair<Transform, IFingerprint> changes = coord.apply(id,
          current.getFingerprint(), transform);
      Transform catchup = changes.getFirst();
      String newSource = transform.call(current.getText());
      if (catchup != null)
        newSource = catchup.call(newSource);
      this.current = PROVIDER.newDocument(newSource);
      assertEquals(changes.getSecond(), current.getFingerprint());
    }

  }

  private class ChangeChecker implements DocumentCoordinator.IListener {

    private final DocumentCoordinator coord;
    private String text;
    private int changeCount = 0;

    public ChangeChecker(DocumentCoordinator coord) {
      this.coord = coord;
      this.text = coord.getCurrent().getText();
    }

    @Override
    public void onChanged(Transform transform) {
      this.text = transform.call(this.text);
      assertEquals(coord.getCurrent().getText(), this.text);
      changeCount++;
    }

  }

  @Test
  public void testRandomTransformations() {
    ExtraRandom random = new ExtraRandom(434253);
    DocumentCoordinator coord = new DocumentCoordinator(PROVIDER,
        random.nextWord(100));
    ChangeChecker checker = new ChangeChecker(coord);
    coord.addListener(checker);
    List<Transformer> transformers = Factory.newArrayList();
    for (int i = 0; i < 4; i++)
      transformers.add(new Transformer(i, coord, random));
    for (int i = 0; i < 1000; i++) {
      Transformer next = random.nextElement(transformers);
      next.fireChange();
    }
    assertEquals(1000, checker.changeCount);
  }

}
