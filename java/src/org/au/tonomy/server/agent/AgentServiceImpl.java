package org.au.tonomy.server.agent;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.au.tonomy.shared.agent.AgentService;
import org.au.tonomy.shared.agent.AgentService.GetFileRootsParameters;
import org.au.tonomy.shared.agent.AgentService.ListFilesParameters;
import org.au.tonomy.shared.agent.AgentService.ReadFileParameters;
import org.au.tonomy.shared.agent.AgentService.StartSessionParameters;
import org.au.tonomy.shared.agent.DocumentData;
import org.au.tonomy.shared.agent.FileData;
import org.au.tonomy.shared.agent.SessionData;
import org.au.tonomy.shared.ot.Md5Fingerprint;
import org.au.tonomy.shared.ot.PojoDocument;
import org.au.tonomy.shared.util.Factory;
import org.au.tonomy.shared.util.Promise;

/**
 * Dispatches api requests.
 */
public class AgentServiceImpl implements AgentService.IServer {

  private final FileSystem fileSystem = new FileSystem(
      PojoDocument.newProvider(Md5Fingerprint.getProvider()),
      Arrays.asList(new File("/Users/plesner/Documents/autonomy/java/test/org/au/tonomy/shared/syntax/testdata")));
  private final Map<String, Session> sessions = Factory.newHashMap();
  private int nextSessionId = 0;

  @Override
  public Promise<SessionData> startSession(StartSessionParameters params) {
    String href = params.getHref();
    System.out.println("Starting session with " + href + ".");
    final String id = genSessionId();
    return Promise.of(getOrCreateSession(id).toPlankton());
  }

  /**
   * Returns the session with the given id or creates it if it doesn't
   * exist.
   */
  private Session getOrCreateSession(String id) {
    Session current = sessions.get(id);
    if (current == null) {
      current = new Session(id, fileSystem);
      sessions.put(id, current);
    }
    return current;
  }

  @Override
  public Promise<List<FileData>> getFileRoots(GetFileRootsParameters params) {
    String sessionId = params.getSessionId();
    Session session = sessions.get(sessionId);
    List<FileData> result = Factory.newArrayList();
    for (SessionFile file : session.getRoots())
      result.add(file.toPlankton());
    return Promise.of(result);
  }

  @Override
  public Promise<? extends List<FileData>> listFiles(ListFilesParameters params) {
    int fileId = params.getFileId();
    String sessionId = params.getSessionId();
    Session session = sessions.get(sessionId);
    List<FileData> result = Factory.newArrayList();
    for (SessionFile file : session.listFiles(fileId))
      result.add(file.toPlankton());
    return Promise.of(result);
  }

  @Override
  public Promise<DocumentData> readFile(ReadFileParameters params) {
    int fileId = params.getFileId();
    String sessionId = params.getSessinId();
    Session session = sessions.get(sessionId);
    return Promise.of(session.readFile(fileId).toPlankton());
  }

  private synchronized String genSessionId() {
    return Integer.toHexString(nextSessionId++);
  }

}
