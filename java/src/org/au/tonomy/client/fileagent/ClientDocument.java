package org.au.tonomy.client.fileagent;

import java.util.Map;

import org.au.tonomy.shared.ot.IDocument;
import org.au.tonomy.shared.ot.IFingerprint;
/**
 * A document backed by json.
 */
public class ClientDocument implements IDocument {

  private final String text;
  private final IFingerprint fingerprint;

  protected ClientDocument(Map<?, ?> map) {
    this.text = (String) map.get("text");
    this.fingerprint = new ClientFingerprint((Map<?, ?>) map.get("fingerprint"));

  }

  @Override
  public IFingerprint getFingerprint() {
    return this.fingerprint;
  }

  @Override
  public String getText() {
    return this.text;
  }

}
