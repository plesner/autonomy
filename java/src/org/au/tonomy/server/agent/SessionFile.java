package org.au.tonomy.server.agent;

import java.util.List;

import org.au.tonomy.shared.ot.Transform;
import org.au.tonomy.shared.plankton.IPlanktonable;
import org.au.tonomy.shared.util.Factory;
import org.au.tonomy.shared.util.IPlanktonFactory;

/**
 * The file information specific to a single session.
 */
public class SessionFile implements IPlanktonable {

  private final Session sessionData;
  private final int id;
  private final SharedFile shared;
  private Transform pendingChanges;

  public SessionFile(Session sessionData, int id, SharedFile shared) {
    this.sessionData = sessionData;
    this.id = id;
    this.shared = shared;
  }

  @Override
  public Object toPlankton(IPlanktonFactory factory) {
    return factory
        .newMap()
        .set("id", id)
        .set("path", shared.getFullPath())
        .set("name", shared.getShortName());
  }

  public List<SessionFile> listFiles() {
    List<SessionFile> result = Factory.newArrayList();
    for (SharedFile child : shared.getChildren())
      result.add(this.sessionData.getOrCreateFile(child));
    return result;
  }

  public SharedFile getShared() {
    return shared;
  }

  public void change(Transform transform) {
    pendingChanges = (pendingChanges == null)
        ? transform
        : pendingChanges.compose(transform);
  }

  public void savePendingChanges() {
    if (pendingChanges == null)
      return;
    shared.apply(pendingChanges);
    shared.save();
    pendingChanges = null;
  }

}
