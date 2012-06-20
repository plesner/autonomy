package org.au.tonomy.shared.syntax;

import java.util.ArrayList;
import java.util.List;

import org.au.tonomy.shared.syntax.MacroParser.State;
import org.au.tonomy.shared.syntax.Token.Type;

/**
 * Parses a sequence of tokens into a syntax tree.
 */
public class Parser {

  private static final Token EOF = Token.error();

  private MacroParser macroParser;
  private final List<Token> tokens;
  private int cursor = 0;

  private Parser(MacroParser macroParser, List<Token> tokens) {
    this.macroParser = macroParser;
    this.tokens = tokens;
  }

  private boolean hasMore() {
    return cursor < tokens.size();
  }

  private Token getCurrent() {
    return hasMore() ? tokens.get(cursor) : EOF;
  }

  private void advance() {
    cursor++;
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

  private SyntaxError newSyntaxError() {
    return new SyntaxError(getCurrent());
  }

  private Ast parseBlockBody() throws SyntaxError {
    List<Ast> exprs = new ArrayList<Ast>();
    while (hasMore() && !at(Type.RBRACE)) {
      Ast next = parseStatement();
      exprs.add(next);
    }
    return Ast.Block.create(exprs);
  }

  private boolean at(Type type) {
    return getCurrent().is(type);
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
      return parseAtomicExpression(expectSemi);
    }
  }

  private void checkSemi(boolean expectSemi) throws SyntaxError {
    if (expectSemi)
      expect(Type.SEMI);
  }

  private Ast parseStatement() throws SyntaxError {
    return parseExpression(true);
  }

  private Ast parseAtomicExpression(boolean expectSemi) throws SyntaxError {
    switch (getCurrent().getType()) {
    case DOLLAR: {
      expect(Type.DOLLAR);
      String word = expect(Type.WORD);
      checkSemi(expectSemi);
      return new Ast.Identifier(word);
    }
    case LPAREN: {
      expect(Type.LPAREN);
      Ast result = parseExpression(false);
      expect(Type.RPAREN);
      checkSemi(expectSemi);
      return result;
    }
    case LBRACE: {
      expect(Type.LBRACE);
      Ast result = parseBlockBody();
      expect(Type.RBRACE);
      return result;
    }
    default:
      throw newSyntaxError();
    }
  }

  public static Ast parse(MacroParser keywordParser, List<Token> tokens) throws SyntaxError {
    return new Parser(keywordParser, tokens).parseBlockBody();
  }

}
