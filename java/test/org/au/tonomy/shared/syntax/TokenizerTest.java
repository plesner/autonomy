package org.au.tonomy.shared.syntax;

import static org.au.tonomy.shared.syntax.Token.ether;
import static org.au.tonomy.shared.syntax.Token.identifier;
import static org.au.tonomy.shared.syntax.Token.number;
import static org.au.tonomy.shared.syntax.Token.operator;
import static org.au.tonomy.shared.syntax.Token.punctuation;
import static org.au.tonomy.shared.syntax.Token.word;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.au.tonomy.shared.syntax.Token.Type;
import org.junit.Test;


public class TokenizerTest extends TestCase {

  private void runScanTest(String str, Token... tokens) {
    List<Token> expected = new ArrayList<Token>();
    boolean first = true;
    for (Token token : tokens) {
      if (first) first = false;
      else expected.add(ether(" "));
      expected.add(token);
    }
    List<Token> found = Tokenizer.tokenize(str);
    assertEquals(expected, found);
  }

  @Test
  public void testScanning() {
    runScanTest("for $i in 0 .. 10",
        word("for"), identifier("$i"), word("in"),
        number("0"), operator(".."), number("10"));
    runScanTest("+ - * /",
        operator("+"), operator("-"), operator("*"), operator("/"));
    runScanTest("( ) { } ; # @", punctuation(Type.LPAREN), punctuation(Type.RPAREN),
        punctuation(Type.LBRACE), punctuation(Type.RBRACE),
        punctuation(Type.SEMI), punctuation(Type.HASH), punctuation(Type.AT));
  }

}
