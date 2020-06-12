package fdit.dsl.xtext.standalone;

import com.google.inject.Binder;
import com.google.inject.name.Names;
import fdit.dsl.AttackScenarioGrammarRuntimeModule;
import fdit.dsl.ide.contentassist.antlr.AttackScenarioGrammarParser;
import fdit.dsl.ide.contentassist.antlr.internal.InternalAttackScenarioGrammarLexer;
import org.eclipse.xtext.conversion.IValueConverterService;
import org.eclipse.xtext.ide.LexerIdeBindings;
import org.eclipse.xtext.ide.editor.contentassist.IdeContentProposalCreator;
import org.eclipse.xtext.ide.editor.contentassist.IdeContentProposalProvider;
import org.eclipse.xtext.ide.editor.contentassist.antlr.IContentAssistParser;
import org.eclipse.xtext.ide.editor.contentassist.antlr.internal.Lexer;
import org.eclipse.xtext.ide.editor.syntaxcoloring.ISemanticHighlightingCalculator;
import org.eclipse.xtext.parser.antlr.ISyntaxErrorMessageProvider;

class AttackScenarioModule extends AttackScenarioGrammarRuntimeModule {

    @Override
    public void configure(final Binder binder) {
        binder.bind(Lexer.class).annotatedWith(Names.named(LexerIdeBindings.CONTENT_ASSIST)).to(
                InternalAttackScenarioGrammarLexer.class);
        binder.bind(IdeContentProposalProvider.class).to(AttackScenarioContentProposalProvider.class);
        binder.bind(IdeContentProposalCreator.class).to(AttackScenarioProposalCreator.class);
        binder.bind(IContentAssistParser.class).to(AttackScenarioGrammarParser.class);
        binder.bind(ISemanticHighlightingCalculator.class).to(AttackScenarioHighlightingCalculator.class);
        binder.bind(ISyntaxErrorMessageProvider.class).to(AttackScenarioErrorMessageProvider.class);
        super.configure(binder);
    }

    @Override
    public Class<? extends IValueConverterService> bindIValueConverterService() {
        return AttackScenarioTerminalConverters.class;
    }
}