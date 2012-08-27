package org.au.tonomy.agent;

import java.util.List;

import org.au.tonomy.shared.util.Factory;
import org.au.tonomy.shared.util.IJsonFactory;
import org.au.tonomy.shared.util.IJsonable;

/**
 * The file information specific to a single session.
 */
public class SessionFile implements IJsonable {

  private final Session sessionData;
  private final int id;
  private final SharedFile shared;

  public SessionFile(Session sessionData, int id, SharedFile shared) {
    this.sessionData = sessionData;
    this.id = id;
    this.shared = shared;
  }

  @Override
  public Object toJson(IJsonFactory factory) {
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

}