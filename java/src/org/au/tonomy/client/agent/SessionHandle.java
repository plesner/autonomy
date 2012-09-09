package org.au.tonomy.client.agent;

import java.util.List;

import org.au.tonomy.shared.agent.AgentService.GetFileRootsParameters;
import org.au.tonomy.shared.agent.FileData;
import org.au.tonomy.shared.agent.SessionData;
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
    return agent.getClient().getFileRoots(
        GetFileRootsParameters
            .newBuilder()
            .setSessionId(data.getId())
            .build())
        .then(new IFunction<List<FileData>, List<FileHandle>>() {
          @Override
          public List<FileHandle> call(List<FileData> files) {
            List<FileHandle> result = Factory.newArrayList();
            for (FileData file : files)
              result.add(new FileHandle(agent, SessionHandle.this, file));
            return result;
          }
        });
  }

}
