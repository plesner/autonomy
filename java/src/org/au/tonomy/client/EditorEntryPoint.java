package org.au.tonomy.client;

import java.util.List;

import org.au.tonomy.client.filesystem.LocalFile;
import org.au.tonomy.client.presentation.CodeEditorPresenter;
import org.au.tonomy.client.util.Callback;
import org.au.tonomy.client.widget.EditorWidget;
import org.au.tonomy.shared.syntax.Ast;
import org.au.tonomy.shared.syntax.MacroParser;
import org.au.tonomy.shared.syntax.Parser;
import org.au.tonomy.shared.syntax.SyntaxError;
import org.au.tonomy.shared.syntax.Token;
import org.au.tonomy.shared.syntax.Tokenizer;
import org.au.tonomy.shared.util.Exceptions;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;

public class EditorEntryPoint implements EntryPoint {

  private static final String FILE = "/Users/plesner/Documents/autonomy/java/test/org/au/tonomy/shared/syntax/testdata/lambdas.aut";

  @Override
  public void onModuleLoad() {
    Panel root = RootPanel.get();
    EditorWidget widget = new EditorWidget();
    root.add(widget);
    final CodeEditorPresenter presenter = new CodeEditorPresenter(widget);
    LocalFile file = LocalFile.forPath(FILE);
    file.getContents().onResolved(new Callback<String>() {
      @Override
      public void onSuccess(String source) {
        List<Token> tokens = Tokenizer.tokenize(source);
        Ast ast;
        try {
          ast = Parser.parse(new MacroParser(), tokens);
        } catch (SyntaxError se) {
          throw Exceptions.propagate(se);
        }
        presenter.getSourceManager().setSource(ast);
      }
    });
  }

}
