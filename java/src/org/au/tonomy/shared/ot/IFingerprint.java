package org.au.tonomy.shared.ot;

import org.au.tonomy.shared.plankton.IPlanktonable;
import org.au.tonomy.shared.plankton.gen.PFingerprint;



/**
 * A unique identifier for a piece of text.
 */
public interface IFingerprint extends IPlanktonable<PFingerprint> {

  public interface IProvider {

    /**
     * Returns the fingerprint for this source code.
     */
    public IFingerprint calcFingerprint(String text);

    /**
     * Creates a fingerprint object form a serialized json object.
     */
    public IFingerprint fromJson(Object json);

  }

  /**
   * Returns the type of this fingerprint.
   */
  public String getVariant();

  /**
   * Returns this fingerprint's payload.
   */
  public String getPayload();

}
