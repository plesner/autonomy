package org.au.tonomy.shared.ot;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.au.tonomy.shared.util.Assert;
import org.au.tonomy.shared.util.Exceptions;
import org.au.tonomy.shared.util.IJsonFactory;
import org.au.tonomy.shared.util.Misc;

/**
 * A fingerprint based on the MD5 hash of the string.
 */
public class Md5Fingerprint implements IFingerprint {

  private final String hash;

  private Md5Fingerprint(String hash) {
    this.hash = Assert.notNull(hash);
    Assert.equals(32, hash.length());
  }

  @Override
  public Object toJson(IJsonFactory factory) {
    return hash;
  }

  /**
   * Calculates a MD5 fingerprint object for the given string.
   */
  public static Md5Fingerprint calc(String text) {
    MessageDigest digest;
    try {
      digest = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException nsae) {
      throw Exceptions.propagate(nsae);
    }
    byte[] textBytes;
    try {
      textBytes = text.getBytes("UTF8");
    } catch (UnsupportedEncodingException uee) {
      throw Exceptions.propagate(uee);
    }
    byte[] bytes = digest.digest(textBytes);
    StringBuilder buf = new StringBuilder();
    for (int i = 0; i < bytes.length; i++)
      Misc.writeHexDigits(bytes[i], 2, buf);
    return new Md5Fingerprint(buf.toString());
  }

  /**
   * Returns the singleton MD5 fingerprint provider.
   */
  public static IProvider getProvider() {
    return PROVIDER;
  }

  /**
   * The singleton MD5 fingerprint provider.
   */
  private static final IProvider PROVIDER = new IProvider() {

    @Override
    public IFingerprint fromJson(Object json) {
      return new Md5Fingerprint((String) json);
    }

    @Override
    public IFingerprint calcFingerprint(String text) {
      return calc(text);
    }

  };

  @Override
  public int hashCode() {
    return hash.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof Md5Fingerprint)) {
      return false;
    } else {
      return this.hash.equals(((Md5Fingerprint) obj).hash);
    }
  }

  @Override
  public String toString() {
    return "MD5[" + hash + "]";
  }

}
