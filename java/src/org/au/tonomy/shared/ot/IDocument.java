package org.au.tonomy.shared.ot;

import org.au.tonomy.shared.agent.pton.DocumentData;
import org.au.tonomy.shared.plankton.IPlanktonable;


/**
 * A document is a piece of text along with a unique id.
 */
public interface IDocument extends IPlanktonable<DocumentData> {

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
