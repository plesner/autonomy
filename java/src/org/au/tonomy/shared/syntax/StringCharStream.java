package org.au.tonomy.shared.syntax;

public class StringCharStream implements ICharStream {

  private final String source;
  private int cursor;
  private char current;

  public StringCharStream(String source) {
    this.source = source;
    this.cursor = -1;
    advance();
  }

  @Override
  public boolean hasMore() {
    return cursor < source.length();
  }

  @Override
  public char getCurrent() {
    return current;
  }

  @Override
  public char getNext() {
    return (cursor + 1 < source.length()) ? source.charAt(cursor + 1) : '\0';
  }

  @Override
  public void advance() {
    this.cursor++;
    this.current = hasMore() ? source.charAt(this.cursor) : '\0';
  }

  @Override
  public int getCursor() {
    return cursor;
  }

  @Override
  public String substring(int start, int end) {
    return source.substring(start, end);
  }

}
