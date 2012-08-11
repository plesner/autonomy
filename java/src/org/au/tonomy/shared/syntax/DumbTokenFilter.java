package org.au.tonomy.shared.syntax;

import java.util.List;

import org.au.tonomy.shared.util.Assert;
import org.au.tonomy.shared.util.Factory;
import org.au.tonomy.shared.util.Internal;

/**
 * A really stupid implementation of a token filter that tokenizes the
 * whole before and after input and calculates the differences from
 * there.
 */
public class DumbTokenFilter implements ITokenFilter<Token> {

  private List<Token> currentTokens = Factory.newArrayList();
  private String source = "";
  private final List<ITokenListener<Token>> listeners = Factory.newArrayList();

  @Override
  public void addListener(ITokenListener<Token> listener) {
    listeners.add(listener);
  }

  @Override
  public void append(String str) {
    updateSource(source + str);
  }

  @Override
  public void insert(int offset, String str) {
    updateSource(source.substring(0, offset) + str + source.substring(offset));
  }

  @Override
  public void delete(int from, int length) {
    Assert.that(from < source.length());
    Assert.that(from + length <= source.length());
    updateSource(source.substring(0, from) + source.substring(from + length));
  }

  @Override
  public void replace(int from, int length, String replacement) {
    Assert.that(from < source.length());
    Assert.that(from + length <= source.length());
    updateSource(source.substring(0, from) + replacement + source.substring(from + length));
  }

  @Internal
  public String getSource() {
    return this.source;
  }

  private void updateSource(String newSource) {
    this.source = newSource;
    fireEvents();
  }

  private void fireEvents() {
    List<Token> newTokens = Tokenizer.tokenize(source, Token.getFactory());
    int firstDifference = findFirstDifference(currentTokens, newTokens);
    int lastDifference = findLastDifference(currentTokens, newTokens, firstDifference);
    // Find the range within the current string that's affected.
    int currentFirstOffset = firstDifference;
    int currentLastOffset = currentTokens.size() - lastDifference;
    Assert.that(currentFirstOffset <= currentLastOffset);
    // Find the range within the new string that's affected.
    int newFirstOffset = firstDifference;
    int newLastOffset = newTokens.size() - lastDifference;
    Assert.that(newFirstOffset <= newLastOffset);
    List<Token> removed = currentTokens.subList(currentFirstOffset, currentLastOffset);
    List<Token> inserted = newTokens.subList(newFirstOffset, newLastOffset);
    if (removed.isEmpty()) {
      if (inserted.isEmpty()) {
        // No changes -- nothing to do.
        Assert.that(currentTokens.equals(newTokens));
      } else {
        insertAndNotify(firstDifference, inserted);
      }
    } else {
      if (inserted.isEmpty()) {
        removeAndNotify(firstDifference, removed);
      } else {
        replaceAndNotify(firstDifference, removed, inserted);
      }
    }
    Assert.equals(newTokens, currentTokens);
  }

  private void insertAndNotify(int offset, List<Token> inserted) {
    for (ITokenListener<Token> listener : listeners)
      listener.onInsert(offset, inserted);
    currentTokens.addAll(offset, inserted);
  }

  private void removeAndNotify(int offset, List<Token> removed) {
    for (ITokenListener<Token> listener : listeners)
      listener.onRemove(offset, removed);
    currentTokens.subList(offset, offset + removed.size()).clear();
  }

  private void replaceAndNotify(int offset, List<Token> removed,
      List<Token> inserted) {
    for (ITokenListener<Token> listener : listeners)
      listener.onReplace(offset, removed, inserted);
    currentTokens.subList(offset, offset + removed.size()).clear();
    currentTokens.addAll(offset, inserted);
  }

  /**
   * Finds the index of the first token that is different between the
   * before and after lists. If the lists are equal it returns the
   * length of the lists (which must be equal since otherwise they
   * wouldn't be equal).
   */
  @Internal
  public static int findFirstDifference(List<Token> before, List<Token> after) {
    int offset = 0;
    while (offset < before.size() && offset < after.size()) {
      if (before.get(offset).equals(after.get(offset))) {
        offset++;
      } else {
        return offset;
      }
    }
    return offset;
  }

  /**
   * Finds the index of the last token that is different between the
   * before and after lists. If the lists are equal it returns 0.
   */
  @Internal
  public static int findLastDifference(List<Token> before, List<Token> after,
      int firstDifference) {
    int reverseOffset = 0;
    while (reverseOffset < before.size() && reverseOffset < after.size()) {
      int beforeOffset = before.size() - reverseOffset - 1;
      int afterOffset = after.size() - reverseOffset - 1;
      if (beforeOffset < firstDifference || afterOffset < firstDifference) {
        return reverseOffset;
      }
      if (before.get(beforeOffset).equals(after.get(afterOffset))) {
        reverseOffset++;
      } else {
        return reverseOffset;
      }
    }
    return reverseOffset;
  }

}
