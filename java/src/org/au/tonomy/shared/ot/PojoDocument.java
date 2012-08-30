package org.au.tonomy.shared.ot;

import org.au.tonomy.shared.plankton.IPlanktonable;
import org.au.tonomy.shared.util.IPlanktonFactory;

/**
 * A plain old java document object.
 */
public class PojoDocument implements IDocument, IPlanktonable {

  private final String text;
  private final IFingerprint fingerprint;

  public PojoDocument(String text, IFingerprint fingerprint) {
    this.text = text;
    this.fingerprint = fingerprint;
  }

  @Override
  public String getText() {
    return this.text;
  }

  @Override
  public IFingerprint getFingerprint() {
    return this.fingerprint;
  }

  /**
   * Returns a document provider that produces pojo documents, using
   * the given fingerprint provider to generate fingerprints.
   */
  public static IProvider newProvider(final IFingerprint.IProvider fingerprinter) {
    return new IProvider() {
      @Override
      public IDocument newDocument(String text) {
        return new PojoDocument(text, fingerprinter.calcFingerprint(text));
      }
    };
  }

  @Override
  public Object toPlankton(IPlanktonFactory factory) {
    return factory.newMap()
        .set("fingerprint", getFingerprint())
        .set("text", getText());
  }

}
