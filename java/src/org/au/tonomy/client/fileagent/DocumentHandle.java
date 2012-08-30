package org.au.tonomy.client.fileagent;

import java.util.Map;

import org.au.tonomy.shared.ot.IFingerprint;
import org.au.tonomy.shared.ot.IMutableDocument;
import org.au.tonomy.shared.ot.Transform;
/**
 * A document backed by json.
 */
public class DocumentHandle implements IMutableDocument {

  private final String text;
  private final IFingerprint fingerprint;
  private final FileHandle file;

  protected DocumentHandle(Map<?, ?> map, FileHandle file) {
    this.text = (String) map.get("text");
    this.fingerprint = new FingerprintHandle((Map<?, ?>) map.get("fingerprint"));
    this.file = file;
  }

  @Override
  public IFingerprint getFingerprint() {
    return this.fingerprint;
  }

  @Override
  public String getText() {
    return this.text;
  }

  public void apply(Transform transform) {
    file.apply(transform);
  }

}
