package org.au.tonomy.client.codemirror;

public class AutonomyMode implements IMode<Void> {

  @Override
  public String getName() {
    return "autonomy";
  }

  @Override
  public Void newStartState() {
    return null;
  }

  @Override
  public String getNextToken(Stream stream, Void state) {
    return null;
  }

}
