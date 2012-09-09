package org.au.tonomy.client.agent;

import org.au.tonomy.shared.agent.DocumentData;
import org.au.tonomy.shared.ot.IFingerprint;
import org.au.tonomy.shared.ot.IMutableDocument;
import org.au.tonomy.shared.ot.Transform;
import org.au.tonomy.shared.util.Assert;

public class DocumentHandle implements IMutableDocument {

  private final DocumentData data;
  private final FileHandle file;

  public DocumentHandle(DocumentData data, FileHandle file) {
    this.data = data;
    this.file = file;
  }

  @Override
  public IFingerprint getFingerprint() {
    return data.getFingerprint();
  }

  @Override
  public String getText() {
    return data.getText();
  }

  @Override
  public void apply(Transform transform) {
    Assert.notNull(file);
  }

  @Override
  public DocumentData toPlankton() {
    return data;
  }

}
