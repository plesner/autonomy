package org.au.tonomy.client.fileagent;

import org.au.tonomy.shared.ot.IFingerprint;
import org.au.tonomy.shared.ot.IMutableDocument;
import org.au.tonomy.shared.ot.Transform;
import org.au.tonomy.shared.plankton.gen.PDocument;

public class DocumentHandle implements IMutableDocument {

  private final PDocument data;
  private final FileHandle file;

  public DocumentHandle(PDocument data, FileHandle file) {
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
    file.apply(transform);
  }

  @Override
  public PDocument toPlankton() {
    return data;
  }

}
