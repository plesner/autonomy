package org.au.tonomy.shared.ot;

import org.au.tonomy.shared.plankton.IPlanktonable;
import org.au.tonomy.shared.util.Assert;
import org.au.tonomy.shared.util.IPlanktonFactory;

/**
 * A text operation.
 */
public abstract class Operation implements IPlanktonable {

  /**
   * Identifies the different types of operations.
   */
  public enum Type {
    INSERT,
    DELETE,
    SKIP
  }

  /**
   * Returns the type of this operation.
   */
  public abstract Type getType();

  /**
   * Dispatch on this operation, which is the first operation in the
   * second in stream. We dispatch on the second operation first so that
   * the first operation is the one that will actually execute the
   * transformation, which makes the code more straightforward.
   */
  public abstract void xformDispatchSecond(OperationStream aIn,
      TransformBuilder aOut, OperationStream bIn,
      TransformBuilder bOut);

  /**
   * Transform this operation against an insertion.
   */
  protected abstract void xformInsert(Insert that, OperationStream aIn,
      TransformBuilder aOut, OperationStream bIn,
      TransformBuilder bOut);

  /**
   * Transform this operation against a deletion.
   */
  protected abstract void xformDelete(Delete that, OperationStream aIn,
      TransformBuilder aOut, OperationStream bIn,
      TransformBuilder bOut);

  /**
   * Transform this operation against a skip.
   */
  protected abstract void xformSkip(Skip that, OperationStream aIn,
      TransformBuilder aOut, OperationStream bIn,
      TransformBuilder bOut);

  /**
   * Applies this operation to the out streams assuming that the
   * other sequence of operations is done.
   */
  public abstract void xformFlush(TransformBuilder thisOut,
      TransformBuilder thatOut);

  /**
   * Dispatch on this operation, which is the first operation in the
   * second in stream. We dispatch on the second operation first so that
   * the first operation is the one that will actually execute the
   * composition, which makes the code more straightforward.
   */
  public abstract void composeDispatchSecond(OperationStream aIn,
      OperationStream bIn, TransformBuilder out);

  /**
   * Compose this operation with an insertion.
   */
  protected abstract void composeInsert(Insert insert, OperationStream aIn,
      OperationStream bIn, TransformBuilder out);

  /**
   * Compose this operation with a deletion.
   */
  protected abstract void composeDelete(Delete delete, OperationStream aIn,
      OperationStream bIn, TransformBuilder out);

  /**
   * Compose this operation with a skip.
   */
  protected abstract void composeSkip(Skip skip, OperationStream aIn,
      OperationStream bIn, TransformBuilder out);

  /**
   * Returns the inverse operation to this one, one that cancels the
   * effect if applied to the result of applying this operation.
   */
  public abstract Operation getInverse();

  /**
   * Applies this operation to an actual input string.
   */
  public abstract void apply(StringInput in, StringBuilder out);

  /**
   * Applies this operation to a transform output.
   */
  public abstract void apply(TransformBuilder out);

  /**
   * Returns the length of the string generated by this operation.
   */
  public abstract int getOutputLength();

  /**
   * Returns the length of the input string consumed by applying this
   * operation.
   */
  public abstract int getInputLength();

  /**
   * Insert a piece of text at the current cursor.
   */
  public static class Insert extends Operation {

    private final String text;

    public Insert(String text) {
      Assert.that(!text.isEmpty());
      this.text = text;
    }

    @Override
    public Type getType() {
      return Type.INSERT;
    }

    @Override
    public void apply(StringInput in, StringBuilder out) {
      out.append(text);
    }

    @Override
    public void apply(TransformBuilder out) {
      out.insert(text);
    }

    @Override
    public Operation getInverse() {
      return new Delete(text);
    }

    @Override
    public int getOutputLength() {
      return text.length();
    }

    @Override
    public int getInputLength() {
      return 0;
    }

    @Override
    public void xformDispatchSecond(OperationStream aIn, TransformBuilder aOut,
        OperationStream bIn, TransformBuilder bOut) {
      aIn.getCurrent().xformInsert(this, aIn, aOut, bIn, bOut);
    }

    @Override
    protected void xformInsert(Insert bOp, OperationStream aIn,
        TransformBuilder aOut, OperationStream bIn,
        TransformBuilder bOut) {
      Insert aOp = this;
      int minLength = Math.min(aOp.text.length(), bOp.text.length());
      for (int i = 0; i < minLength; i++) {
        char aFirst = aOp.text.charAt(i);
        char bFirst = bOp.text.charAt(i);
        if (aFirst == bFirst) {
          aOut.skip(1);
          bOut.skip(1);
        } else if (aFirst < bFirst) {
          // A's character is lexically first so we first insert it,
          // then b's,
          aOut.insert(Character.toString(aFirst));
          aOut.skip(1);
          bOut.skip(1);
          bOut.insert(Character.toString(bFirst));
        } else {
          // B's character is lexically first so we first insert it,
          // then a's,
          bOut.insert(Character.toString(bFirst));
          bOut.skip(1);
          aOut.skip(1);
          aOut.insert(Character.toString(aFirst));
        }
      }
      // Finally we advance the stream that contained the shortest
      // string and update the operation of the other if it is longer.
      if (minLength == text.length()) {
        aIn.advance();
      } else {
        aIn.setCurrent(new Insert(aOp.text.substring(minLength)));
      }
      if (minLength == bOp.text.length()) {
        bIn.advance();
      } else {
        bIn.setCurrent(new Insert(bOp.text.substring(minLength)));
      }
    }

    @Override
    protected void xformDelete(Delete bOp, OperationStream aIn,
        TransformBuilder aOut, OperationStream bIn,
        TransformBuilder bOut) {
      xformDeleteInsert(bOp, this, bIn, bOut, aIn, aOut);
    }

    @Override
    protected void xformSkip(Skip bOp, OperationStream aIn,
        TransformBuilder aOut, OperationStream bIn,
        TransformBuilder bOut) {
      xformSkipInsert(bOp, this, bIn, bOut, aIn, aOut);
    }

    @Override
    public void xformFlush(TransformBuilder thisOut, TransformBuilder thatOut) {
      thatOut.skip(text.length());
      thisOut.insert(text);
    }

    @Override
    public void composeDispatchSecond(OperationStream aIn,
        OperationStream bIn, TransformBuilder out) {
      aIn.getCurrent().composeInsert(this, aIn, bIn, out);
    }

    @Override
    protected void composeInsert(Insert insert, OperationStream aIn,
        OperationStream bIn, TransformBuilder out) {
      out.insert(insert.text);
      bIn.advance();
    }

    @Override
    protected void composeDelete(Delete delete, OperationStream aIn,
        OperationStream bIn, TransformBuilder out) {
      if (text.length() == delete.text.length()) {
        // We're deleting exactly the text being inserted so these cancel
        // out.
        aIn.advance();
        bIn.advance();
      } else if (text.length() < delete.text.length()) {
        // We're deleting more than this text. So delete this first
        // and let the rest of the delete compose with the next operation.
        aIn.advance();
        bIn.setCurrent(new Delete(delete.text.substring(text.length())));
      } else {
        // We're deleting less than is being inserted. So just cancel
        // that part out.
        aIn.setCurrent(new Insert(text.substring(delete.text.length())));
        bIn.advance();
      }
    }

    @Override
    protected void composeSkip(Skip skip, OperationStream aIn,
        OperationStream bIn, TransformBuilder out) {
      if (skip.count == text.length()) {
        // We're skipping exactly the text being inserted.
        out.insert(text);
        aIn.advance();
        bIn.advance();
      } else if (skip.count < text.length()) {
        // We're skipping only part of the text being inserted. So
        // insert just that part.
        out.insert(text.substring(0, skip.count));
        aIn.setCurrent(new Insert(text.substring(skip.count)));
        bIn.advance();
      } else {
        // We're skipping more than is being inserted.
        out.insert(text);
        aIn.advance();
        bIn.setCurrent(new Skip(skip.count - text.length()));
      }
    }

    @Override
    public Object toPlankton(IPlanktonFactory factory) {
      return factory.newArray().push("+").push(text);
    }

    @Override
    public int hashCode() {
      return text.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      } else if (!(obj instanceof Insert)) {
        return false;
      } else {
        return this.text.equals(((Insert) obj).text);
      }
    }

    @Override
    public String toString() {
      return "+\"" + text + "\"";
    }

  }

  /**
   * Delete the specified text, starting at the current cursor.
   */
  public static class Delete extends Operation {

    private final String text;

    public Delete(String text) {
      Assert.that(!text.isEmpty());
      this.text = text;
    }

    @Override
    public Type getType() {
      return Type.DELETE;
    }

    @Override
    public void apply(StringInput in, StringBuilder out) {
      in.skip(text);
    }

    @Override
    public void apply(TransformBuilder out) {
      out.delete(text);
    }

    @Override
    public int getOutputLength() {
      return 0;
    }

    @Override
    public int getInputLength() {
      return text.length();
    }

    @Override
    public Operation getInverse() {
      return new Insert(text);
    }

    @Override
    public void xformDispatchSecond(OperationStream aIn,
        TransformBuilder aOut, OperationStream bIn,
        TransformBuilder bOut) {
      aIn.getCurrent().xformDelete(this, aIn, aOut, bIn, bOut);
    }

    @Override
    protected void xformInsert(Insert bOp, OperationStream aIn,
        TransformBuilder aOut, OperationStream bIn,
        TransformBuilder bOut) {
      xformDeleteInsert(this, bOp, aIn, aOut, bIn, bOut);
    }

    @Override
    protected void xformDelete(Delete bOp, OperationStream aIn,
        TransformBuilder aOut, OperationStream bIn,
        TransformBuilder bOut) {
      Delete aOp = this;
      int minLength = Math.min(aOp.text.length(), bOp.text.length());
      for (int i = 0; i < minLength; i++) {
        char aFirst = aOp.text.charAt(i);
        char bFirst = bOp.text.charAt(i);
        if (aFirst == bFirst) {
          // Both have the same effect so we don't have to do anything.
        } else if (aFirst < bFirst) {
          // A's character is lexically first so we first delete it,
          // then b's,
          aOut.delete(Character.toString(aFirst));
          bOut.delete(Character.toString(bFirst));
        } else {
          // B's character is lexically first so we first delete it,
          // then a's,
          bOut.delete(Character.toString(bFirst));
          aOut.delete(Character.toString(aFirst));
        }
      }
      if (minLength == aOp.text.length()) {
        aIn.advance();
      } else {
        aIn.setCurrent(new Delete(aOp.text.substring(minLength)));
      }
      if (minLength == bOp.text.length()) {
        bIn.advance();
      } else {
        bIn.setCurrent(new Delete(bOp.text.substring(minLength)));
      }
    }

    @Override
    protected void xformSkip(Skip bOp, OperationStream aIn,
        TransformBuilder aOut, OperationStream bIn,
        TransformBuilder bOut) {
      xformSkipDelete(bOp, this, bIn, bOut, aIn, aOut);
    }

    @Override
    public void xformFlush(TransformBuilder source, TransformBuilder target) {
      source.delete(text);
    }

    @Override
    public void composeDispatchSecond(OperationStream aIn,
        OperationStream bIn, TransformBuilder out) {
      aIn.getCurrent().composeDelete(this, aIn, bIn, out);
    }

    @Override
    protected void composeInsert(Insert insert, OperationStream aIn,
        OperationStream bIn, TransformBuilder out) {
      out.delete(this.text);
      aIn.advance();
    }

    @Override
    protected void composeDelete(Delete delete, OperationStream aIn,
        OperationStream bIn, TransformBuilder out) {
      out.delete(this.text);
      aIn.advance();
    }

    @Override
    protected void composeSkip(Skip skip, OperationStream aIn,
        OperationStream bIn, TransformBuilder out) {
      out.delete(text);
      aIn.advance();
    }

    @Override
    public Object toPlankton(IPlanktonFactory factory) {
      return factory.newArray().push("-").push(text);
    }

    @Override
    public int hashCode() {
      return text.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      } else if (!(obj instanceof Delete)) {
        return false;
      } else {
        return this.text.equals(((Delete) obj).text);
      }
    }

    @Override
    public String toString() {
      return "-\"" + text + "\"";
    }

  }

  /**
   * Skip over the next n characters.
   */
  public static class Skip extends Operation {

    private final int count;

    public Skip(int count) {
      Assert.that(count > 0);
      this.count = count;
    }

    @Override
    public Type getType() {
      return Type.SKIP;
    }

    @Override
    public void apply(StringInput in, StringBuilder out) {
      out.append(in.scan(count));
    }

    @Override
    public void apply(TransformBuilder out) {
      out.skip(count);
    }

    @Override
    public Operation getInverse() {
      return this;
    }

    @Override
    public int getOutputLength() {
      return count;
    }

    @Override
    public int getInputLength() {
      return count;
    }

    @Override
    public void xformDispatchSecond(OperationStream aIn, TransformBuilder aOut,
        OperationStream bIn, TransformBuilder bOut) {
      aIn.getCurrent().xformSkip(this, aIn, aOut, bIn, bOut);
    }

    @Override
    protected void xformInsert(Insert bOp, OperationStream aIn,
        TransformBuilder aOut, OperationStream bIn,
        TransformBuilder bOut) {
      xformSkipInsert(this, bOp, aIn, aOut, bIn, bOut);
    }

    @Override
    protected void xformDelete(Delete bOp, OperationStream aIn,
        TransformBuilder aOut, OperationStream bIn,
        TransformBuilder bOut) {
      xformSkipDelete(this, bOp, aIn, aOut, bIn, bOut);
    }

    @Override
    protected void xformSkip(Skip bOp, OperationStream aIn,
        TransformBuilder aOut, OperationStream bIn,
        TransformBuilder bOut) {
      Skip aOp = this;
      if (aOp.count == bOp.count) {
        // If they have the same effect there's nothing to do.
        aOut.skip(aOp.count);
        bOut.skip(bOp.count);
        aIn.advance();
        bIn.advance();
      } else if (aOp.count < bOp.count) {
        // If a skips less than b then we skip that much ahead and then
        // compose the rest of b's skip with whatever comes after in a.
        aOut.skip(aOp.count);
        bOut.skip(aOp.count);
        aIn.advance();
        bIn.setCurrent(new Skip(bOp.count - aOp.count));
      } else {
        // And conversely when b skips less then a.
        aOut.skip(bOp.count);
        bOut.skip(bOp.count);
        bIn.advance();
        aIn.setCurrent(new Skip(aOp.count - bOp.count));
      }
    }

    @Override
    public void xformFlush(TransformBuilder thisOut, TransformBuilder thatOut) {
      thisOut.skip(count);
    }

    @Override
    public void composeDispatchSecond(OperationStream aIn,
        OperationStream bIn, TransformBuilder out) {
      aIn.getCurrent().composeSkip(this, aIn, bIn, out);
    }

    @Override
    protected void composeInsert(Insert insert, OperationStream aIn,
        OperationStream bIn, TransformBuilder out) {
      out.insert(insert.text);
      bIn.advance();
    }

    @Override
    protected void composeDelete(Delete delete, OperationStream aIn,
        OperationStream bIn, TransformBuilder out) {
      if (count == delete.text.length()) {
        // The distance skipped is exactly the text being deleted.
        out.delete(delete.text);
        aIn.advance();
        bIn.advance();
      } else if (count < delete.text.length()) {
        // We're skipping less than is being deleted so just delete
        // the part that corresponds to the skip and consume the
        // skip.
        out.delete(delete.text.substring(0, count));
        bIn.setCurrent(new Delete(delete.text.substring(count)));
        aIn.advance();
      } else {
        // We're skipping more than is being deleted so just cut out
        // the part that's being deleted from the skip.
        out.delete(delete.text);
        aIn.setCurrent(new Skip(count - delete.text.length()));
        bIn.advance();
      }
    }

    @Override
    protected void composeSkip(Skip skip, OperationStream aIn,
        OperationStream bIn, TransformBuilder out) {
      if (count == skip.count) {
        out.skip(count);
        aIn.advance();
        bIn.advance();
      } else if (count < skip.count) {
        out.skip(count);
        aIn.advance();
        bIn.setCurrent(new Skip(skip.count - count));
      } else {
        out.skip(skip.count);
        aIn.setCurrent(new Skip(count - skip.count));
        bIn.advance();
      }
    }

    @Override
    public Object toPlankton(IPlanktonFactory factory) {
      return factory.newArray().push(">").push(count);
    }

    @Override
    public int hashCode() {
      return count;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      } else if (!(obj instanceof Skip)) {
        return false;
      } else {
        return this.count == ((Skip) obj).count;
      }
    }

    @Override
    public String toString() {
      return ">" + count;
    }

  }

  private static void xformSkipInsert(Skip skip, Insert insert, OperationStream skipIn,
      TransformBuilder skipOut, OperationStream insertIn,
      TransformBuilder insertOut) {
    // If text has been inserted in the part we're skipping we have
    // to skip the new text too.
    skipOut.skip(insert.text.length());
    insertOut.insert(insert.text);
    insertIn.advance();
  }

  private static void xformSkipDelete(Skip skip, Delete delete,
      OperationStream skipIn, TransformBuilder skipOut,
      OperationStream deleteIn, TransformBuilder deleteOut) {
    if (skip.count > delete.text.length()) {
      // We're skipping more than is being deleted. Consume the deleted
      // part of the skip and just execute the deletion.
      skipIn.setCurrent(new Skip(skip.count - delete.text.length()));
      deleteOut.delete(delete.text);
      deleteIn.advance();
    } else {
      // We're the same or less than is being deleted so the skip gets
      skipIn.advance();
      deleteOut.delete(delete.text.substring(0, skip.count));
      if (skip.count < delete.text.length()) {
        deleteIn.setCurrent(new Delete(delete.text.substring(skip.count)));
      } else {
        deleteIn.advance();
      }
    }
  }

  private static void xformDeleteInsert(Delete delete, Insert insert,
      OperationStream deleteIn, TransformBuilder deleteOut,
      OperationStream insertIn, TransformBuilder insertOut) {
    // Just ignore the deletion and let it compose with the next
    // operation.
    insertOut.insert(insert.text);
    insertIn.advance();
    // The deletion should only be applied after the insertion so skip
    // the text.
    deleteOut.skip(insert.text.length());
  }

}
