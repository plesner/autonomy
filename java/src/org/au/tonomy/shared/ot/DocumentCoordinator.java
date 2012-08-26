package org.au.tonomy.shared.ot;

import java.util.List;
import java.util.Map;

import org.au.tonomy.shared.util.Assert;
import org.au.tonomy.shared.util.Factory;
import org.au.tonomy.shared.util.Pair;

/**
 * A coordinator that can be given multiple streams of document operations
 * and will merge them into a single stream which it will notify listeners
 * of changes to.
 */
public class DocumentCoordinator {

  private Document document;
  private final IFingerprint.IProvider fingerprinter;
  private final Map<IFingerprint, Integer> lastSeen = Factory.newHashMap();
  private final List<State> history = Factory.newArrayList();

  public DocumentCoordinator(IFingerprint.IProvider fingerprinter, String text) {
    this.fingerprinter = fingerprinter;
    IFingerprint fprint = fingerprinter.calcFingerprint(text);
    this.document = new Document(text, fprint);
    lastSeen.put(fprint, -1);
  }

  /**
   * Returns the current document state.
   */
  public Document getCurrent() {
    return this.document;
  }

  /**
   * Applies a transformation to the current stats, assuming that the
   * transform is relative to the document with the given parent
   * fingerprint.
   *
   * @param id an identifier for this change. It will not be used by
   *   the transformation but will be passed along with the transform
   *   when passed to listeners.
   * @param parentPrint fingerprint of the document the transform is
   *   constructed relative to.
   * @param transform the transformation to apply.
   * @return A pair containing the transformation that can be applied
   *   to the document the transform is relative to produce the current
   *   document, and the fingerprint of the current document.
   */
  public Pair<Transform, IFingerprint> apply(String id, IFingerprint parentPrint, Transform transform) {
    // Find the place in the history where this change is based.
    Integer lastSeenIndex = lastSeen.get(parentPrint);
    Assert.notNull(lastSeenIndex);
    // Scan through all the transformations that have been applied
    // since and transform the change against them, at the same time
    // as building the transformation that we'll return.
    Transform current = transform;
    Transform result = null;
    for (int i = lastSeenIndex + 1; i < history.size(); i++) {
      State state = history.get(i);
      Pair<Transform, Transform> adapted = current.xform(state.transform);
      current = adapted.getFirst();
      result = (result == null)
          ? adapted.getSecond()
          : result.compose(adapted.getSecond());
    }
    String newText = current.call(document.getText());
    IFingerprint fprint = fingerprinter.calcFingerprint(newText);
    this.lastSeen.put(fprint, history.size());
    this.history.add(new State(current, id));
    this.document = new Document(newText, fprint);
    return Pair.of(result, fprint);
  }

  /**
   * The state of the document at a particular time.
   */
  private static class State {

    private final Transform transform;
    private final String id;

    public State(Transform transform, String id) {
      this.transform = transform;
      this.id = id;
    }

  }

}
