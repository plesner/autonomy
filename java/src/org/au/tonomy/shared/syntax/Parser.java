package org.au.tonomy.shared.syntax;

import java.util.Collections;
import java.util.List;

import org.au.tonomy.shared.runtime.IntegerValue;
import org.au.tonomy.shared.syntax.IToken.IFactory;
import org.au.tonomy.shared.syntax.IToken.Type;
import org.au.tonomy.shared.syntax.MacroParser.Component;
import org.au.tonomy.shared.syntax.MacroParser.Placeholder;
import org.au.tonomy.shared.syntax.MacroParser.State;
import org.au.tonomy.shared.util.Factory;

/**
 * Parses a sequence of tokens into a syntax tree.
 */
public class Parser {

  /**
   * Marker that identifies, while parsing a block, whether it is a
   * nested block or the program toplevel.
   */
  private enum NestingLevel {
    TOPLEVEL, LOCAL
  }

  private static final String DEF = "def";
  private static final String FN = "fn";

  private MacroParser macroParser;
  private final IFactory<?> tokenFactory;
  private final List<? extends IToken> tokens;
  private final OperatorRegistry operators = new OperatorRegistry();

  private int cursor = 0;
  private IToken prev;
  private IToken current;

  private Parser(MacroParser macroParser, IFactory<?> tokenFactory, List<? extends IToken> tokens) {
    this.macroParser = macroParser;
    this.tokenFactory = tokenFactory;
    this.tokens = tokens;
    current = tokenFactory.newEof();
    skipEther();
  }

  private boolean hasMore() {
    return cursor < tokens.size();
  }

  private IToken getCurrent() {
    return current;
  }

  private void skipEther() {
    prev = current;
    while (hasMore() && tokens.get(cursor).is(Type.ETHER))
      cursor++;
    if (hasMore())
      current = tokens.get(cursor);
    else
      current = tokenFactory.newEof();
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

  /**
   * Skips over the current operator which must have the specified value.
   */
  private void expectOperator(String value) throws SyntaxError {
    if (atOperator(value)) {
      advance();
    } else {
      throw newSyntaxError();
    }
  }

  private SyntaxError newSyntaxError() {
    return new SyntaxError(getCurrent());
  }

  private Ast parseBlockBody(NestingLevel level, Type endMarker) throws SyntaxError {
    List<Ast> exprs = Factory.newArrayList();
    while (hasMore() && !at(endMarker)) {
      Ast next = parseStatement(level, endMarker);
      exprs.add(next);
    }
    return Ast.Block.create(exprs);
  }

  private boolean at(Type type) {
    return getCurrent().is(type);
  }

  private boolean atWord(String value) {
    IToken current = getCurrent();
    return current.is(Type.WORD) && value.equals(current.getValue());
  }

  private boolean atOperator(String value) {
    IToken current = getCurrent();
    return current.is(Type.OPERATOR) && value.equals(current.getValue());
  }

  private boolean lastWas(Type type) {
    return prev.is(type);
  }

  private Ast parseExpression(boolean expectSemi) throws SyntaxError {
    if (atWord(FN)) {
      return parseLambda(expectSemi);
    } else if (at(Type.LBRACE)) {
      return parseBlockExpression();
    } else {
      return parseMacroExpression(expectSemi);
    }
  }

  private Ast parseLambda(boolean expectSemi) throws SyntaxError {
    expectWord("fn");
    return parseLambdaTail(expectSemi);
  }

  private Ast parseLambdaTail(boolean expectSemi) throws SyntaxError {
    List<String> params = parseParameters();
    Ast body = parseFunctionBody(expectSemi);
    return new Ast.Lambda(params, body);
  }

  private Ast parseFunctionBody(boolean expectSemi) throws SyntaxError {
    if (atOperator("=>")) {
      expectOperator("=>");
      return parseExpression(expectSemi);
    } else {
      return parseBlockExpression();
    }
  }

  private List<String> parseParameters() throws SyntaxError {
    if (at(Type.LPAREN)) {
      expect(Type.LPAREN);
      if (at(Type.RPAREN)) {
        expect(Type.RPAREN);
        return Collections.emptyList();
      } else {
        List<String> result = Factory.newArrayList();
        result.add(expect(Type.IDENTIFIER));
        while (hasMore() && at(Type.COMMA)) {
          expect(Type.COMMA);
          result.add(expect(Type.IDENTIFIER));
        }
        expect(Type.RPAREN);
        return result;
      }
    } else {
      return Collections.emptyList();
    }
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
      Ast value = parseExpression(expectSemi && !state.suppressSemi());
      return continueMacroExpression(state.advance(value), expectSemi);
    } else if (state != null && state.isFinal()) {
      // If we can't advance the state but it's a final state then
      // that's fine.
      checkSemi(expectSemi && !state.getMacro().suppressEndSemi());
      return state.build();
    } else {
      // If we can't do anything we give up on macros and just parse
      // this as an atomic expression.
      return parseOperatorExpression(expectSemi);
    }
  }

  private PrecedenceParser<AstOrArguments> newPrecedenceParser() {
    return new PrecedenceParser<AstOrArguments>(operators, Ast.Call.FACTORY);
  }

  /**
   * Parse a compact expression like calls and indexing.
   */
  private Ast parseOperatorExpression(boolean expectSemi) throws SyntaxError {
    PrecedenceParser<AstOrArguments> parser = newPrecedenceParser();
    boolean lastWasOperand = false;
    while (hasMore()) {
      if (at(Type.OPERATOR)) {
        String op = expect(Type.OPERATOR);
        parser.addOperator(op);
        lastWasOperand = false;
      } else if (atAtomicStart()) {
        if (lastWasOperand) {
          if (at(Type.LPAREN)) {
            parser.addOperator("()");
          } else if (at(Type.LBRACK)) {
            parser.addOperator("[]");
          } else {
            throw newSyntaxError();
          }
        }
        AstOrArguments operand = parseAtomicExpression();
        parser.addOperand(operand);
        lastWasOperand = true;
      } else {
        break;
      }
    }
    checkSemi(expectSemi);
    return parser.flush().asAst();
  }

  private void checkSemi(boolean expectSemi) throws SyntaxError {
    if (expectSemi && !lastWas(Type.RBRACE))
      expect(Type.SEMI);
  }

  private Ast parseStatement(NestingLevel level, Type endMarker) throws SyntaxError {
    List<Ast> annots = Collections.emptyList();
    while (at(Type.AT)) {
      expect(Type.AT);
      AstOrArguments annot = parseAtomicExpression();
      if (annots.isEmpty())
        annots = Factory.newArrayList();
      annots.add(annot.asAst());
    }
    if (atWord(DEF)) {
      return parseDefinition(level, annots, endMarker);
    } else {
      return parseExpression(true);
    }
  }

  private Ast parseDefinition(NestingLevel level, List<Ast> annots,
      Type endMarker) throws SyntaxError {
    expectWord(DEF);
    if (at(Type.LBRACK)) {
      return parseMacroDefinition(annots, endMarker);
    } else {
      return parsePlainDefinition(level, annots, endMarker);
    }
  }

  private Ast parseMacroDefinition(List<Ast> annots, Type endMarker) throws SyntaxError {
    Macro macro = parseMacroHeader();
    MacroParser oldParser = macroParser;
    MacroParser newParser = new MacroParser(macroParser);
    newParser.addSequence(macro);
    Ast rest;
    try {
      macroParser = newParser;
      rest = parseBlockBody(NestingLevel.LOCAL, endMarker);
    } finally {
      macroParser = oldParser;
    }
    return new Ast.LocalDefinition(annots, macro.getId(), macro.getBody(), rest);
  }

  private Macro parseMacroHeader() throws SyntaxError {
    // Parse the header
    expect(Type.LBRACK);
    List<MacroParser.Component> components = Factory.newArrayList();
    List<String> params = Factory.newArrayList();
    while (hasMore() && !at(Type.RBRACK)) {
      Component next;
      if (at(Type.WORD)) {
        String word = expect(Type.WORD);
        next = new MacroParser.Keyword(word);
      } else {
        String param = expect(Type.IDENTIFIER);
        params.add(param);
        Placeholder.Type type;
        if (at(Type.LPAREN)) {
          type = Placeholder.Type.LAZY_EXPRESSION;
          expect(Type.LPAREN);
          expect(Type.RPAREN);
        } else {
          type = Placeholder.Type.EAGER_EXPRESSION;
        }
        next = new MacroParser.Placeholder(type);
      }
      components.add(next);
    }
    expect(Type.RBRACK);
    // Parse the body
    Ast body = parseFunctionBody(true);
    Macro macro = new Macro(components, new Ast.Lambda(params, body));
    return macro;
  }

  private Ast parsePlainDefinition(NestingLevel level, List<Ast> annots,
      Type endMarker) throws SyntaxError {
    String name = expect(Type.IDENTIFIER);
    Ast value;
    if (at(Type.ASSIGN)) {
      expect(Type.ASSIGN);
      value = parseExpression(true);
    } else {
      value = parseLambdaTail(true);
    }
    Ast body = parseBlockBody(level, endMarker);
    if (level == NestingLevel.TOPLEVEL) {
      return new Ast.ToplevelDefinition(annots, name, value, body);
    } else {
      return new Ast.LocalDefinition(annots, name, value, body);
    }
  }

  private Ast parseBlockExpression() throws SyntaxError {
    expect(Type.LBRACE);
    Ast result = parseBlockBody(NestingLevel.LOCAL, Type.RBRACE);
    expect(Type.RBRACE);
    return result;
  }

  /**
   * Can the current token start an atomic expression?
   */
  private boolean atAtomicStart() {
    switch (getCurrent().getType()) {
    case IDENTIFIER: case NUMBER: case LPAREN: case LBRACK: case HASH:
      return true;
    default:
      return false;
    }
  }

  /**
   * Parses the current atomic expression.
   */
  private AstOrArguments parseAtomicExpression() throws SyntaxError {
    switch (getCurrent().getType()) {
    case IDENTIFIER: {
      String word = expect(Type.IDENTIFIER);
      return new Ast.Identifier(word);
    }
    case LPAREN: {
      List<Ast> elms = parseArguments(Type.LPAREN, Type.RPAREN);
      return Ast.Arguments.create(elms);
    }
    case LBRACK: {
      List<Ast> elms = parseArguments(Type.LBRACK, Type.RBRACK);
      return Ast.Arguments.create(elms);
    }
    case HASH: {
      expect(Type.HASH);
      List<Ast> elms = parseArguments(Type.LBRACK, Type.RBRACK);
      return new Ast.Tuple(elms);
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
      List<Ast> asts = Factory.newArrayList();
      asts.add(parseExpression(false));
      while (at(Type.COMMA)) {
        expect(Type.COMMA);
        asts.add(parseExpression(false));
      }
      expect(end);
      return asts;
    }
  }

  public static Ast parse(MacroParser keywordParser, IFactory<?> tokenFactory,
      List<? extends IToken> tokens) throws SyntaxError {
    return new Parser(keywordParser, tokenFactory, tokens).parseBlockBody(NestingLevel.TOPLEVEL, Type.EOF);
  }

}
