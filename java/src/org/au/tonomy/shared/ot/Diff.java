package org.au.tonomy.shared.ot;

import java.util.Map;

import org.au.tonomy.shared.util.Assert;
import org.au.tonomy.shared.util.Factory;


/**
 * Utility for producing a transformation that turns one string into another,
 * given the two strings. Based on the diff algorithm from
 * "An O(ND) Difference Algorithm and Its Variations" by Eugene W. Myers,
 * http://citeseerx.ist.psu.edu/viewdoc/summary?doi=10.1.1.4.6927.
 */
public class Diff {

  /**
   * Returns a transform that produces 'after' when applied to 'before'.
   */
  public static Transform diff(String before, String after) {
    EditPathNode path = buildPath(before, after);
    DiffTransformBuilder out = new DiffTransformBuilder();
    writeTransform(path, before, after, out);
    out.catchUp(before.length());
    return out.flush();
  }

  /**
   * Builds the edit path between the two strings.
   */
  private static EditPathNode buildPath(String before, String after) {
    int maxPathLength = before.length() + after.length() + 1;
    MirrorArray<EditPathNode> diagonal = new MirrorArray<EditPathNode>(maxPathLength);
    diagonal.set(1, newDummy());
    for (int d = 0; d < maxPathLength; d++) {
      for (int k = -d; k <= d; k += 2) {
        EditPathNode prev = null;
        int beforeDelta = 0;
        if (k == -d) {
          prev = diagonal.get(k + 1);
        } else if (k == d) {
          prev = diagonal.get(k - 1);
          beforeDelta = 1;
        } else {
          EditPathNode pred = diagonal.get(k - 1);
          EditPathNode succ = diagonal.get(k + 1);
          if (pred.getBeforeOffset() < succ.getBeforeOffset()) {
            prev = succ;
          } else  {
            prev = pred;
            beforeDelta = 1;
          }
        }
        int beforeOffset = prev.getBeforeOffset() + beforeDelta;
        diagonal.clear(k);
        int afterOffset = beforeOffset - k;
        EditPathNode node = newLink(beforeOffset, afterOffset, prev);
        while (beforeOffset < before.length()
            && afterOffset < after.length()
            && before.charAt(beforeOffset) == after.charAt(afterOffset)) {
          beforeOffset++;
          afterOffset++;
        }
        if (beforeOffset > node.getBeforeOffset())
          node = newSnake(beforeOffset, afterOffset, node);
        diagonal.set(k, node);
        if (beforeOffset >= before.length() && afterOffset >= after.length())
          return diagonal.get(k);
      }
      diagonal.clear(d - 1);
    }
    Assert.that(false);
    return null;
  }

  /**
   * Build the transformation from an edit path, writing it to the
   * given diff builder.
   */
  private static void writeTransform(EditPathNode end, String before,
      String after, DiffTransformBuilder out) {
    // Check if we're at the beginning.
    if (end == null)
      return;
    EditPathNode start = end.getPrevious();
    if (start == null || start.getAfterOffset() < 0)
      return;
    // Make sure everything that comes before has already been written.
    writeTransform(end.getPrevious(), before, after, out);
    // Snakes represent overlap so we don't have to explicitly transform
    // those.
    if (!end.isSnake()) {
      // Catch up to the beginning of this change if necessary.
      out.catchUp(start.getBeforeOffset());
      if (start.getBeforeOffset() < end.getBeforeOffset())
        out.delete(before.substring(end.getPrevious().getBeforeOffset(), end.getBeforeOffset()));
      if (start.getAfterOffset() < end.getAfterOffset())
        out.insert(after.substring(end.getPrevious().getAfterOffset(), end.getAfterOffset()));
    }
  }

  /**
   * A single node in the edit path.
   */
  public static class EditPathNode {

    private static final int DUMMY_AFTER_OFFSET = -1;

    private final int beforeOffset;
    private final int afterOffset;
    private final EditPathNode prev;
    private final boolean isSnake;

    public EditPathNode(int beforeOffset, int afterOffset, EditPathNode prev,
        boolean isSnake) {
      this.beforeOffset = beforeOffset;
      this.afterOffset = afterOffset;
      this.prev = prev;
      this.isSnake = isSnake;
    }

    /**
     * Returns the previous node.
     */
    public EditPathNode getPrevious() {
      return this.prev;
    }

    /**
     * Is this part of a snake?
     */
    public boolean isSnake() {
      return isSnake;
    }

    /**
     * Is this the dummy start node?
     */
    public boolean isDummy() {
      return this.afterOffset == DUMMY_AFTER_OFFSET;
    }

    /**
     * Returns the offset in the after string of this node.
     */
    public int getAfterOffset() {
      return this.afterOffset;
    }

    /**
     * Returns the offset in the before string of this node.
     */
    public int getBeforeOffset() {
      return this.beforeOffset;
    }

  }

  /**
   * Returns a new snake node.
   */
  private static EditPathNode newSnake(int beforeOffset, int afterOffset,
      EditPathNode prev) {
    return new EditPathNode(beforeOffset, afterOffset, prev, true);
  }

  /**
   * Returns a new dummy node.
   */
  private static EditPathNode newDummy() {
    return newSnake(0, EditPathNode.DUMMY_AFTER_OFFSET, null);
  }

  /**
   * Returns a new non-snake node.
   */
  private static EditPathNode newLink(int beforeOffset, int afterOffset,
      EditPathNode prev) {
    // Scan backwards until we find a snake or the beginning of the
    // input.
    EditPathNode candidate = prev;
    while (candidate != null) {
      if (candidate.isDummy()) {
        candidate = null;
      } else if (!candidate.isSnake() && candidate.getPrevious() != null) {
        candidate = candidate.getPrevious();
      } else {
        break;
      }
    }
    return new EditPathNode(beforeOffset, afterOffset, candidate, false);
  }

  /**
   * A sparse array with indexes from -K to K.
   */
  private static class MirrorArray<T> {

    private final int length;
    private final Map<Integer, T> elms = Factory.newHashMap();

    public MirrorArray(int length) {
      this.length = length;
    }

    /**
     * Returns the index'th entry.
     */
    public T get(int index) {
      Assert.that(Math.abs(index) < length);
      return Assert.notNull(elms.get(index));
    }

    /**
     * Sets the value of the index'th element.
     */
    public void set(int index, T value) {
      Assert.that(Math.abs(index) < length);
      elms.put(index, Assert.notNull(value));
    }

    /**
     * Clears the given index from this array.
     */
    public void clear(int index) {
      Assert.that(Math.abs(index) < length);
      elms.remove(index);
    }

  }

  /**
   * A transform builder with a "hanging" cursor that can fall behind
   * and catch up as necessary by inserting skips.
   */
  private static class DiffTransformBuilder extends TransformBuilder {

    private int cursor = 0;

    @Override
    public void delete(String text) {
      super.delete(text);
      cursor += text.length();
    }

    @Override
    public void skip(int count) {
      super.skip(count);
      cursor += count;
    }

    public void catchUp(int offset) {
      skip(offset - cursor);
    }

  }

}