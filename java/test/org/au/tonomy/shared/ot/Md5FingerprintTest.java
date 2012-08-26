package org.au.tonomy.shared.ot;

import java.util.Random;
import java.util.Set;

import junit.framework.TestCase;

import org.au.tonomy.shared.util.Factory;
import org.junit.Test;

public class Md5FingerprintTest extends TestCase {

  private Random random;

  private String getRandomString(int length) {
    StringBuilder buf = new StringBuilder();
    for (int i = 0; i < length; i++) {
      char c = (char) random.nextInt(256);
      buf.append(c);
    }
    return buf.toString();
  }

  @Test
  public void testRandomizedFingerprints() {
    IFingerprint.IProvider provider = Md5Fingerprint.getProvider();
    Set<String> strings = Factory.newHashSet();
    Set<IFingerprint> fprints = Factory.newHashSet();
    random = new Random(41342);
    for (int i = 0; i < 10; i++) {
      String str = getRandomString(100);
      assertFalse(strings.contains(str));
      strings.add(str);
      IFingerprint fprint = provider.calcFingerprint(str);
      assertFalse(fprints.contains(fprint));
      fprints.add(fprint);
    }
    assertEquals(strings.size(), fprints.size());
    for (String str : strings)
      assertTrue(fprints.contains(provider.calcFingerprint(str)));
  }

}
