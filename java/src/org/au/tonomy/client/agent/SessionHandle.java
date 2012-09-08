package org.au.tonomy.client.agent;

import java.util.List;

import org.au.tonomy.shared.agent.pton.FileData;
import org.au.tonomy.shared.agent.pton.SessionData;
import org.au.tonomy.shared.util.Factory;
import org.au.tonomy.shared.util.IFunction;
import org.au.tonomy.shared.util.Promise;
/**
 * The data that describes a session with the file agent.
 */
public class SessionHandle {

  private final FileAgent agent;
  private final SessionData data;

  public SessionHandle(FileAgent agent, SessionData data) {
    this.agent = agent;
    this.data = data;
  }

  /**
   * Returns the unique identifier for this session.
   */
  public String getId() {
    return data.getId();
  }

  public Promise<List<FileHandle>> getRoots() {
    return agent.newMessage("fileroots")
        .setArgument("session", data.getId())
        .send()
        .then(new IFunction<Object, List<FileHandle>>() {
          @Override
          public List<FileHandle> call(Object arg) {
            List<?> files = (List<?>) arg;
            List<FileHandle> result = Factory.newArrayList();
            for (Object file : files)
              result.add(new FileHandle(agent, SessionHandle.this, FileData.parse(file)));
            return result;
          }
        });
  }

}
