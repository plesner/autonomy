package org.au.tonomy.client.codemirror;

import org.au.tonomy.shared.syntax.ICharStream;
import org.au.tonomy.shared.syntax.IToken.Type;
import org.au.tonomy.shared.syntax.ITokenFactory;
import org.au.tonomy.shared.syntax.Tokenizer;

public class AutonomyMode implements IMode<Object> {

  @Override
  public String getName() {
    return "autonomy";
  }

  @Override
  public Object newStartState() {
    return null;
  }

  @Override
  public String getNextToken(Stream stream, Object state) {
    return new Tokenizer<String>(new EditorCharStream(stream), TOKEN_FACTORY).scanNext();
  }

  private static class EditorCharStream implements ICharStream {

    private final Stream stream;
    private char current;
    private boolean hasMore = true;
    private boolean hasAdvanced = false;

    public EditorCharStream(Stream stream) {
      this.stream = stream;
      advance();
    }

    @Override
    public boolean hasMore() {
      return hasMore;
    }

    @Override
    public char getCurrent() {
      return current;
    }

    @Override
    public char getNext() {
      return '\0';
    }

    @Override
    public void advance() {
      if (hasAdvanced)
        stream.next();
      hasAdvanced = true;
      String next = stream.peek();
      if (next.length() == 0) {
        current = '\0';
        hasMore = false;
      } else {
        current = next.charAt(0);
      }
    }

    @Override
    public int getCursor() {
      return 0;
    }

    @Override
    public String substring(int start, int end) {
      return null;
    }

  }

  /**
   * The singleton factory that maps recognized tokens into strings
   * for formatting.
   */
  private static ITokenFactory<String> TOKEN_FACTORY = new ITokenFactory<String>() {

    @Override
    public String newSpace(String value) {
      return null;
    }

    @Override
    public String newNewline(char value) {
      return null;
    }

    @Override
    public String newComment(String value) {
      return "comment";
    }

    @Override
    public String newWord(String value) {
      return "keyword";
    }

    @Override
    public String newIdentifier(String value) {
      return "variable";
    }

    @Override
    public String newNumber(String value) {
      return "number";
    }

    @Override
    public String newOperator(String value) {
      return "operator";
    }

    @Override
    public String newError(char value) {
      return "error";
    }

    @Override
    public String newEof() {
      return null;
    }

    @Override
    public String newPunctuation(Type type) {
      return null;
    }

  };

}
