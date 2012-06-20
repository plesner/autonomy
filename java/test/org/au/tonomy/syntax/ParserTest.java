package org.au.tonomy.syntax;

import static org.au.tonomy.shared.syntax.MacroParser.Placeholder.Type.EAGER_EXPRESSION;
import static org.au.tonomy.shared.syntax.MacroParser.Placeholder.Type.LAZY_STATEMENT;

import java.util.Arrays;

import junit.framework.TestCase;

import org.au.tonomy.shared.syntax.Ast;
import org.au.tonomy.shared.syntax.Macro;
import org.au.tonomy.shared.syntax.MacroParser;
import org.au.tonomy.shared.syntax.Parser;
import org.au.tonomy.shared.syntax.SyntaxError;
import org.au.tonomy.shared.syntax.Tokenizer;
import org.au.tonomy.shared.syntax.MacroParser.Component;
import org.au.tonomy.shared.syntax.MacroParser.Keyword;
import org.au.tonomy.shared.syntax.MacroParser.Placeholder;
import org.au.tonomy.shared.syntax.MacroParser.Placeholder.Type;
import org.junit.Test;


public class ParserTest extends TestCase {

  private Ast parse(MacroParser keywordParser, String str) throws SyntaxError {
    return Parser.parse(keywordParser, Tokenizer.tokenize(str));
  }

  private void runParserTest(String expected, String str) throws SyntaxError {
    runParserTest(new MacroParser(), expected, str);
  }

  private void runParserTest(MacroParser parser, String expected, String str) throws SyntaxError {
    Ast ast = parse(parser, str);
    assertEquals(expected, ast.toString());
  }

  @Test
  public void testToplevel() throws SyntaxError {
    runParserTest("$a", "$a;");
    runParserTest("$a", "($a);");
    runParserTest("$a", "{$a;}");
    runParserTest("(; $a $b)", "{$a; $b;}");
    runParserTest("(; $a $b $c)", "{$a; $b; {$c;}}");
  }

  private static MacroParser newParser(Macro... macs) {
    MacroParser parser = new MacroParser();
    for (Macro mac : macs)
      parser.addSequence(mac);
    return parser;
  }

  private static Macro mac(Component... comps) {
    return new Macro(Arrays.asList(comps));
  }

  private static MacroParser.Keyword kwd(String str) {
    return new Keyword(str);
  }

  private static MacroParser.Placeholder phd(Type type) {
    return new Placeholder(type);
  }


  @Test
  public void testKeywords() throws SyntaxError {
    // Simple case, just keywords
    MacroParser p0 = newParser(
        mac(kwd("if"), kwd("then"), kwd("else")));
    runParserTest(p0, "(if-then-else)", "if then else;");
    // Simple case with placeholder
    MacroParser p1 = newParser(
        mac(kwd("if"), phd(EAGER_EXPRESSION), kwd("then")));
    runParserTest(p1, "(if-then $a)", "if $a then;");
    runParserTest(p1, "(if-then (; $a $b $c))", "if {$a; $b; $c;} then;");
    runParserTest(p1, "(if-then (if-then $b))", "if (if $b then) then;");
    runParserTest(p1, "(if-then (if-then $b))", "if if $b then then;");
    // Multiple overlapping
    MacroParser p2 = newParser(
        mac(kwd("if"), phd(EAGER_EXPRESSION), kwd("then"), phd(LAZY_STATEMENT)),
        mac(kwd("if"), phd(EAGER_EXPRESSION), kwd("then"), phd(LAZY_STATEMENT), kwd("else"), phd(LAZY_STATEMENT)));
    runParserTest(p2, "(if-then $a $b)", "if $a then $b;");
    runParserTest(p2, "(if-then-else $a $b $c)", "if $a then $b; else $c;");
    runParserTest(p2, "(if-then $a (if-then-else $b $c $d))", "if $a then if $b then $c; else $d;");
    // Semicolons
    runParserTest(p2, "(if-then-else $a $b $c)", "{if $a then $b; else $c;}");
    runParserTest(p2, "(if-then-else $a $b $c)", "(if $a then $b else $c);");
  }

}
