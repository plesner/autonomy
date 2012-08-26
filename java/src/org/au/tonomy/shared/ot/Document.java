package org.au.tonomy.shared.ot;
/**
 * A document is a piece of text along with a unique id.
 */
public class Document {

  private final String text;
  private final IFingerprint fingerprint;

  public Document(String text, IFingerprint fingerprint) {
    this.text = text;
    this.fingerprint = fingerprint;
  }

  /**
   * Returns the source of this document.
   */
  public String getText() {
    return this.text;
  }

  /**
   * Returns the fingerprint of the source.
   */
  public IFingerprint getFingerprint() {
    return this.fingerprint;
  }

}
