package org.au.tonomy.shared.plankton;
/**
 * An error thrown when a remote response is unsuccessful.
 */
@SuppressWarnings("serial")
public class RemoteError extends RuntimeException {

  private final Object error;

  public RemoteError(Object error) {
    super(String.valueOf(error));
    this.error = error;
  }

}
