package autonomy.syntax;

import static autonomy.syntax.Token.number;
import static autonomy.syntax.Token.operator;
import static autonomy.syntax.Token.punctuation;
import static autonomy.syntax.Token.word;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Test;

import autonomy.syntax.Token;
import autonomy.syntax.Tokenizer;
import autonomy.syntax.Token.Type;

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
