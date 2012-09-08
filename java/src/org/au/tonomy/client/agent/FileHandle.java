package org.au.tonomy.client.agent;

import java.util.List;
import java.util.Map;

import org.au.tonomy.shared.agent.pton.DocumentData;
import org.au.tonomy.shared.agent.pton.FileData;
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
  private final FileData data;

  public FileHandle(FileAgent agent, SessionHandle session, FileData data) {
    this.agent = agent;
    this.session = session;
    this.data = data;
  }

  @Override
  public String getFullPath() {
    return data.getPath();
  }

  @Override
  public String getShortName() {
    return data.getName();
  }

  @Override
  public Promise<Map<String, FileHandle>> listEntries() {
    return agent.newMessage("ls")
      .setArgument("file", data.getId())
      .setArgument("session", session.getId())
      .send()
      .then(new IFunction<Object, Map<String, FileHandle>>() {
        @Override
        public Map<String, FileHandle> call(Object value) {
          List<?> files = (List<?>) value;
          Map<String, FileHandle> list = Factory.newHashMap();
          for (Object file : files) {
            FileHandle handle = new FileHandle(agent, session, FileData.parse(file));
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
        .setArgument("file", data.getId())
        .setArgument("session", session.getId())
        .send()
        .then(new IFunction<Object, DocumentHandle>() {
          @Override
          public DocumentHandle call(Object arg) {
            return new DocumentHandle(DocumentData.parse(arg), FileHandle.this);
          }
        });
  }

  public void apply(Transform transform) {
    agent.newMessage("changefile")
        .setArgument("file", data.getId())
        .setArgument("session", session.getId())
        .setArgument("transform", transform)
        .sendAsync();
  }

}
