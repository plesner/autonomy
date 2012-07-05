package org.au.tonomy.shared.syntax;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.au.tonomy.shared.runtime.IntegerValue;
import org.au.tonomy.shared.syntax.Ast.Tuple;
import org.au.tonomy.shared.syntax.MacroParser.State;
import org.au.tonomy.shared.syntax.Token.Type;

/**
 * Parses a sequence of tokens into a syntax tree.
 */
public class Parser {

  private static final Token EOF = Token.eof();

  private MacroParser macroParser;
  private final List<Token> tokens;
  private int cursor = 0;
  private Token prev;
  private Token current;

  private Parser(MacroParser macroParser, List<Token> tokens) {
    this.macroParser = macroParser;
    this.tokens = tokens;
    current = EOF;
    skipEther();
  }

  private boolean hasMore() {
    return cursor < tokens.size();
  }

  private Token getCurrent() {
    return current;
  }

  private void skipEther() {
    prev = current;
    while (hasMore() && tokens.get(cursor).is(Type.ETHER))
      cursor++;
    if (hasMore())
      current = tokens.get(cursor);
    else
      current = EOF;
  }

  private void advance() {
    cursor++;
    skipEther();
  }

  /**
   * Skips over the current token of the specified type, returning the
   * value. If the type doesn't match throws an error.
   */
  private String expect(Type type) throws SyntaxError {
    if (at(type)) {
      String value = getCurrent().getValue();
      advance();
      return value;
    } else {
      throw newSyntaxError();
    }
  }

  /**
   * Skips over the current word which must have the specified value.
   */
  private void expectWord(String word) throws SyntaxError {
    if (atWord(word)) {
      advance();
    } else {
      throw newSyntaxError();
    }
  }

  private SyntaxError newSyntaxError() {
    return new SyntaxError(getCurrent());
  }

  private Ast parseBlockBody(Type endMarker) throws SyntaxError {
    List<Ast> exprs = new ArrayList<Ast>();
    while (hasMore() && !at(endMarker)) {
      Ast next = parseStatement(endMarker);
      exprs.add(next);
    }
    return Ast.Block.create(exprs);
  }

  private boolean at(Type type) {
    return getCurrent().is(type);
  }

  private boolean atWord(String value) {
    Token current = getCurrent();
    return current.is(Type.WORD) && value.equals(current.getValue());
  }

  private boolean lastWas(Type type) {
    return prev.is(type);
  }

  private Ast parseExpression(boolean expectSemi) throws SyntaxError {
    return parseMacroExpression(expectSemi);
  }

  /**
   * Parses a macro expression from the beginning.
   */
  private Ast parseMacroExpression(boolean expectSemi) throws SyntaxError {
    return continueMacroExpression(null, expectSemi);
  }

  /**
   * Continues parsing a macro expression at the given state. If the
   * state is null then that means it is the initial state.
   */
  private Ast continueMacroExpression(State state, boolean expectSemi) throws SyntaxError {
    if (at(Type.WORD)) {
      // If we're at a word and we can advance the state by using it
      // we will.
      if (state == null)
        state = macroParser.getInitialState();
      State nextState = state.advance(getCurrent().getValue());
      if (nextState != null) {
        expect(Type.WORD);
        return continueMacroExpression(nextState, expectSemi);
      }
    }
    if (state != null && state.isPlaceholder()) {
      // If we can advance the state by reading a subexpression we
      // do that.
      Ast value = parseMacroExpression(expectSemi && !state.suppressSemi());
      return continueMacroExpression(state.advance(value), expectSemi);
    } else if (state != null && state.isFinal()) {
      // If we can't advance the state but it's a final state then
      // that's fine.
      checkSemi(expectSemi && !state.getMacro().suppressEndSemi());
      return state.build();
    } else {
      // If we can't do anything we give up on macros and just parse
      // this as an atomic expression.
      return parseCompactExpression(expectSemi);
    }
  }

  /**
   * Parse a compact expression like calls and indexing.
   */
  private Ast parseCompactExpression(boolean expectSemi) throws SyntaxError {
    Ast result = parseAtomicExpression();
    if (at(Type.OPERATOR)) {
      String op = expect(Type.OPERATOR);
      List<Ast> args;
      if (at(Type.LPAREN)) {
        args = parseArguments(Type.LPAREN, Type.RPAREN);
      } else if (at(Type.LBRACK)) {
        op += "[]";
        args = parseArguments(Type.LBRACK, Type.RBRACK);
      } else {
        args = Arrays.asList(parseExpression(false));
      }
      result = new Ast.Call(result, op, args);
    }
    checkSemi(expectSemi);
    return result;
  }

  private void checkSemi(boolean expectSemi) throws SyntaxError {
    if (expectSemi && !lastWas(Type.RBRACE))
      expect(Type.SEMI);
  }

  private static final String DEF = "def";

  private Ast parseStatement(Type endMarker) throws SyntaxError {
    if (atWord(DEF)) {
      return parseDefinition(endMarker);
    } else {
      return parseExpression(true);
    }
  }

  private Ast parseDefinition(Type endMarker) throws SyntaxError {
    expectWord(DEF);
    String name = expect(Type.IDENTIFIER);
    expect(Type.ASSIGN);
    Ast value = parseExpression(true);
    Ast body = parseBlockBody(endMarker);
    return new Ast.Definition(name, value, body);
  }

  private Ast parseAtomicExpression() throws SyntaxError {
    switch (getCurrent().getType()) {
    case IDENTIFIER: {
      String word = expect(Type.IDENTIFIER);
      return new Ast.Identifier(word);
    }
    case LPAREN: {
      expect(Type.LPAREN);
      Ast result = parseExpression(false);
      expect(Type.RPAREN);
      return result;
    }
    case LBRACE: {
      expect(Type.LBRACE);
      Ast result = parseBlockBody(Type.RBRACE);
      expect(Type.RBRACE);
      return result;
    }
    case LBRACK: {
      List<Ast> elms = parseArguments(Type.LBRACK, Type.RBRACK);
      return new Tuple(elms);
    }
    case NUMBER: {
      String value = expect(Type.NUMBER);
      return new Ast.Literal(IntegerValue.get(Integer.parseInt(value)));
    }
    default:
      throw newSyntaxError();
    }
  }

  private List<Ast> parseArguments(Type start, Type end) throws SyntaxError {
    expect(start);
    if (at(end)) {
      expect(end);
      return Collections.<Ast>emptyList();
    } else {
      List<Ast> asts = new ArrayList<Ast>();
      asts.add(parseExpression(false));
      while (at(Type.COMMA)) {
        expect(Type.COMMA);
        asts.add(parseExpression(false));
      }
      expect(end);
      return asts;
    }
  }

  public static Ast parse(MacroParser keywordParser, List<Token> tokens) throws SyntaxError {
    return new Parser(keywordParser, tokens).parseBlockBody(Type.EOF);
  }

}
