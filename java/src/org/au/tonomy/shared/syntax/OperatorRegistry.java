package org.au.tonomy.shared.syntax;
/**
 * The default operator registry implementation used by the parser.
 */
public class OperatorRegistry implements IOperatorRegistry {

  @Override
  public boolean preferPrefix(String op) {
    return false;
  }

  @Override
  public Precedence getPrecedence(String a, String b) {
    return Precedence.SAME;
  }

  @Override
  public Associativity getAssociativity(String op) {
    return Associativity.LEFT;
  }

  @Override
  public boolean preferSuffix(String op) {
    return false;
  }

}
