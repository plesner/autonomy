package org.au.tonomy.client.agent;

import java.util.List;
import java.util.Map;

import org.au.tonomy.shared.agent.AgentService.ListFilesParameters;
import org.au.tonomy.shared.agent.AgentService.ReadFileParameters;
import org.au.tonomy.shared.agent.DocumentData;
import org.au.tonomy.shared.agent.FileData;
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
    return agent.getClient().listFiles(
        ListFilesParameters
            .newBuilder()
            .setFileId(data.getId())
            .setSessionId(session.getId())
            .build())
        .then(new IFunction<List<FileData>, Map<String, FileHandle>>() {
          @Override
          public Map<String, FileHandle> call(List<FileData> files) {
            Map<String, FileHandle> list = Factory.newHashMap();
            for (FileData file : files) {
              FileHandle handle = new FileHandle(agent, session, file);
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
    return agent.getClient().readFile(
        ReadFileParameters
            .newBuilder()
            .setFileId(data.getId())
            .setSessinId(session.getId())
            .build())
        .then(new IFunction<DocumentData, DocumentHandle>() {
          @Override
          public DocumentHandle call(DocumentData data) {
            return new DocumentHandle(data, FileHandle.this);
          }
        });
  }

}
