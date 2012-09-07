package org.au.tonomy.client.presentation;

import org.au.tonomy.shared.agent.pton.PDocument;
import org.au.tonomy.shared.ot.IFingerprint;
import org.au.tonomy.shared.ot.IMutableDocument;
import org.au.tonomy.shared.ot.Transform;
/**
 * A fake mutable document that has a text and nothing else.
 */
public class FakeDocument implements IMutableDocument {

  private final String text;

  public FakeDocument(String text) {
    this.text = text;
  }

  @Override
  public IFingerprint getFingerprint() {
    return null;
  }

  @Override
  public String getText() {
    return this.text;
  }

  @Override
  public void apply(Transform transform) {
    // ignore
  }

  @Override
  public PDocument toPlankton() {
    return null;
  }

}
