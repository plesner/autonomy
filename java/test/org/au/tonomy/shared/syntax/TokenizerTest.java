package org.au.tonomy.shared.syntax;

import java.util.List;

import junit.framework.TestCase;

import org.au.tonomy.shared.syntax.IToken.Type;
import org.au.tonomy.shared.util.Factory;
import org.junit.Test;


public class TokenizerTest extends TestCase {

  private void runScanTest(String str, Token... tokens) {
    runScanTest(true, str, tokens);
  }

  private void runRawScanTest(String str, Token... tokens) {
    runScanTest(false, str, tokens);
  }

  private void runScanTest(boolean insertEther, String str, Token[] tokens) {
    List<Token> expected = Factory.newArrayList();
    boolean first = true;
    for (Token token : tokens) {
      if (insertEther) {
        if (first) first = false;
        else expected.add(space(" "));
      }
      expected.add(token);
    }
    List<Token> found = Tokenizer.tokenize(str, Token.getFactory());
    assertEquals(expected, found);
  }

  @Test
  public void testScanning() {
    runScanTest("for $i in 0 -> 10",
        word("for"), identifier("$i"), word("in"),
        number("0"), operator("->"), number("10"));
    runScanTest("+ - * /",
        operator("+"), operator("-"), operator("*"), operator("/"));
    runScanTest("( ) { } ; # @", punctuation(Type.LPAREN), punctuation(Type.RPAREN),
        punctuation(Type.LBRACE), punctuation(Type.RBRACE),
        punctuation(Type.SEMI), punctuation(Type.HASH), punctuation(Type.AT));
    runRawScanTest("## foo", space("## foo"));
    runRawScanTest("## foo\n## bar", space("## foo"), newline('\n'), space("## bar"));
    runRawScanTest("## foo\n## bar", space("## foo"), newline('\n'), space("## bar"));
    runRawScanTest(" \n\n ", space(" "), newline('\n'), newline('\n'), space(" "));
    runRawScanTest("#{ foo }#", comment("#{ foo }#"));
    runRawScanTest("#{ \n }#", comment("#{ "), newline('\n'), comment(" }#"));
    runRawScanTest("#{ \n\n }#", comment("#{ "), newline('\n'), newline('\n'), comment(" }#"));
  }

  private Token punctuation(Type type) {
    return Token.getFactory().newPunctuation(type);
  }

  private Token number(String string) {
    return Token.getFactory().newNumber(string);
  }

  private Token operator(String string) {
    return Token.getFactory().newOperator(string);
  }

  private Token identifier(String string) {
    return Token.getFactory().newIdentifier(string);
  }

  private Token word(String string) {
    return Token.getFactory().newWord(string);
  }

  private static Token space(String string) {
    return Token.getFactory().newSpace(string);
  }

  private static Token newline(char value) {
    return Token.getFactory().newNewline(value);
  }

  private static Token comment(String value) {
    return Token.getFactory().newComment(value);
  }

}
