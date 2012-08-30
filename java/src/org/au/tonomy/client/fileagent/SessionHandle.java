package org.au.tonomy.client.fileagent;

import java.util.List;
import java.util.Map;

import org.au.tonomy.shared.util.Factory;
import org.au.tonomy.shared.util.IFunction;
import org.au.tonomy.shared.util.Promise;
/**
 * The data that describes a session with the file agent.
 */
public class SessionHandle {

  private final String id;
  private final FileAgent agent;

  public SessionHandle(Map<?, ?> data, FileAgent agent) {
    this.agent = agent;
    this.id = (String) data.get("session");
  }

  /**
   * Returns the unique identifier for this session.
   */
  public String getId() {
    return id;
  }

  public Promise<List<FileHandle>> getRoots() {
    return agent.newMessage("fileroots")
        .setArgument("session", id)
        .send()
        .then(new IFunction<Object, List<FileHandle>>() {
          @Override
          public List<FileHandle> call(Object arg) {
            List<?> files = (List<?>) arg;
            List<FileHandle> result = Factory.newArrayList();
            for (Object file : files)
              result.add(new FileHandle((Map<?, ?>) file, agent, SessionHandle.this));
            return result;
          }
        });
  }

}
