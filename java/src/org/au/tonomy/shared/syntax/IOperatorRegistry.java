package org.au.tonomy.shared.syntax;
/**
 * A description of a set of operators.
 */
public interface IOperatorRegistry {

  /**
   * Identifies the relative precedence between two operators.
   */
  public enum Precedence {
    TIGHTER, LOOSER, SAME, UNRELATED
  }

  /**
   * Returns which way an infix operator associates.
   */
  public enum Associativity {
    LEFT, RIGHT
  }

  /**
   * Returns true if this operator binds tighter as a prefix.
   */
  public boolean preferPrefix(String op);

  /**
   * Returns the relative precedence between the two given operator.
   */
  public Precedence getPrecedence(String a, String b);

  /**
   * Returns the associativity of an operator.
   */
  public Associativity getAssociativity(String op);

  /**
   * Returns true if this operator binds tighter as a suffix.
   */
  public boolean preferSuffix(String op);

}
