package org.au.tonomy.shared.syntax;

import java.util.List;

import org.au.tonomy.shared.runtime.Context;
import org.au.tonomy.shared.runtime.Executor;

/**
 * Encapsulates compiling a string into an executable object.
 */
public class Compiler {

  public static Executor compile(Context context, String source) throws SyntaxError {
    List<Token> tokens = Tokenizer.tokenize(source);
    MacroParser macros = new MacroParser();
    Ast ast = Parser.parse(macros, tokens);
    return new Executor(context, ast);
  }

}
