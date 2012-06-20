package org.au.tonomy.syntax;

import static org.au.tonomy.shared.syntax.Token.number;
import static org.au.tonomy.shared.syntax.Token.operator;
import static org.au.tonomy.shared.syntax.Token.punctuation;
import static org.au.tonomy.shared.syntax.Token.word;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.au.tonomy.shared.syntax.Token;
import org.au.tonomy.shared.syntax.Tokenizer;
import org.au.tonomy.shared.syntax.Token.Type;
import org.junit.Test;


public class TokenizerTest extends TestCase {

  private void runScanTest(String str, Token... tokens) {
    List<Token> found = Tokenizer.tokenize(str);
    assertEquals(Arrays.asList(tokens), found);
  }

  @Test
  public void testScanning() {
    runScanTest("for $i in 0 .. 10",
        word("for"), punctuation(Type.DOLLAR), word("i"), word("in"),
        number("0"), operator(".."), number("10"));
    runScanTest("+ - * /",
        operator("+"), operator("-"), operator("*"), operator("/"));
    runScanTest("( ) { } ; # @", punctuation(Type.LPAREN), punctuation(Type.RPAREN),
        punctuation(Type.LBRACE), punctuation(Type.RBRACE),
        punctuation(Type.SEMI), punctuation(Type.HASH), punctuation(Type.AT));
  }

}
