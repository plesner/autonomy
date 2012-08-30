package org.au.tonomy.client.fileagent;

import java.util.List;
import java.util.Map;

import org.au.tonomy.shared.ot.Transform;
import org.au.tonomy.shared.source.ISourceEntry;
import org.au.tonomy.shared.util.Factory;
import org.au.tonomy.shared.util.IFunction;
import org.au.tonomy.shared.util.Promise;
/**
 * A handle to a file connected through the file agent.
 */
public class FileHandle implements ISourceEntry {

  private final FileAgent agent;
  private final SessionHandle session;
  private final int id;
  private final String fullPath;
  private final String shortName;

  public FileHandle(Map<?, ?> data, FileAgent agent, SessionHandle session) {
    this.agent = agent;
    this.session = session;
    this.id = (Integer) data.get("id");
    this.fullPath = (String) data.get("path");
    this.shortName = (String) data.get("name");
  }

  @Override
  public String getFullPath() {
    return fullPath;
  }

  @Override
  public String getShortName() {
    return shortName;
  }

  @Override
  public Promise<Map<String, FileHandle>> listEntries() {
    return agent.newMessage("ls")
      .setArgument("file", id)
      .setArgument("session", session.getId())
      .send()
      .then(new IFunction<Object, Map<String, FileHandle>>() {
        @Override
        public Map<String, FileHandle> call(Object value) {
          List<?> files = (List<?>) value;
          Map<String, FileHandle> list = Factory.newHashMap();
          for (Object file : files) {
            FileHandle handle = new FileHandle((Map<?, ?>) file, agent, session);
            list.put(handle.getShortName(), handle);
          }
          return list;
        }
      });
  }

  /**
   * Returns the contents of this file.
   */
  @Override
  public Promise<DocumentHandle> readFile() {
    return agent.newMessage("read")
        .setArgument("file", id)
        .setArgument("session", session.getId())
        .send()
        .then(new IFunction<Object, DocumentHandle>() {
          @Override
          public DocumentHandle call(Object arg) {
            return new DocumentHandle((Map<?, ?>) arg, FileHandle.this);
          }
        });
  }

  public void apply(Transform transform) {
    agent.newMessage("changefile")
        .setArgument("file", id)
        .setArgument("session", session.getId())
        .setArgument("transform", transform)
        .sendAsync();
  }

}
