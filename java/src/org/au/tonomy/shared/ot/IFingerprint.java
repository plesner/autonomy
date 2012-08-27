package org.au.tonomy.shared.ot;



/**
 * A unique identifier for a piece of text.
 */
public interface IFingerprint {

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

}
