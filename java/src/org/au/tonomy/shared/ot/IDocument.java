package org.au.tonomy.shared.ot;


/**
 * A document is a piece of text along with a unique id.
 */
public interface IDocument {

  /**
   * A document provider that can produce documents from source code.
   */
  public interface IProvider {

    /**
     * Create a new document for the given text.
     */
    public IDocument newDocument(String text);

  }

  /**
   * Returns the fingerprint of the source.
   */
  public IFingerprint getFingerprint();

  /**
   * Returns the source of this document.
   */
  public String getText();

}
