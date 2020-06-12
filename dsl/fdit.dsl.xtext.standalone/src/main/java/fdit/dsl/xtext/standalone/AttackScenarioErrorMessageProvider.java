package fdit.dsl.xtext.standalone;

import com.google.inject.Inject;
import fdit.dsl.services.AttackScenarioGrammarGrammarAccess;
import fdit.tools.i18n.MessageTranslator;
import org.antlr.runtime.MismatchedTokenException;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;
import org.eclipse.xtext.Keyword;
import org.eclipse.xtext.TerminalRule;
import org.eclipse.xtext.nodemodel.SyntaxErrorMessage;
import org.eclipse.xtext.parser.antlr.SyntaxErrorMessageProvider;
import org.eclipse.xtext.xtext.RuleNames;

import java.util.Optional;

import static fdit.tools.i18n.MessageTranslator.createMessageTranslator;
import static fdit.tools.stream.StreamUtils.tryFind;
import static org.eclipse.xtext.GrammarUtil.allTerminalRules;
import static org.eclipse.xtext.diagnostics.Diagnostic.SYNTAX_DIAGNOSTIC;
import static org.eclipse.xtext.xtext.RuleNames.getRuleNames;

public class AttackScenarioErrorMessageProvider extends SyntaxErrorMessageProvider {

    private static final MessageTranslator TRANSLATOR = createMessageTranslator(AttackScenarioErrorMessageProvider.class);

    @Inject
    private AttackScenarioGrammarGrammarAccess grammarAccess;

    private static SyntaxErrorMessage getDefaultSyntaxErrorMessage(final IParserErrorContext context) {
        final Token token = context.getRecognitionException().token;
        if (token.getType() == Token.EOF) {
            return new SyntaxErrorMessage(TRANSLATOR.getMessage("error.unexpectedEOF"),
                    SYNTAX_DIAGNOSTIC);
        }
        return new SyntaxErrorMessage(TRANSLATOR.getMessage("error.unexpectedToken_1",
                token.getText()),
                SYNTAX_DIAGNOSTIC);
    }

    @Override
    public SyntaxErrorMessage getSyntaxErrorMessage(final IParserErrorContext context) {
        if (context.getRecognitionException() == null) {
            return new SyntaxErrorMessage(TRANSLATOR.getMessage("error.unexpectedToken_0"), SYNTAX_DIAGNOSTIC);
        }
        final RecognitionException recognitionException = context.getRecognitionException();
        if (recognitionException instanceof MismatchedTokenException) {
            return getMismatchedErrorMessage((MismatchedTokenException) recognitionException, context);
        }
        return getDefaultSyntaxErrorMessage(context);
    }

    private SyntaxErrorMessage getMismatchedErrorMessage(final MismatchedTokenException mismatchedException,
                                                         final IParserErrorContext context) {
        if (mismatchedException.expecting == Token.EOF) {
            return getDefaultSyntaxErrorMessage(context);
        }
        try {
            final String expectedToken = context.getTokenNames()[mismatchedException.expecting];
            final RuleNames ruleNames = getRuleNames(grammarAccess.getGrammar(), true);
            final String ruleName = ruleNames.getRuleByAntlrName(expectedToken).getName();
            if (ruleName.equals(getIntegerLiteralRuleName())) {
                return new SyntaxErrorMessage(TRANSLATOR.getMessage("error.expectedInteger"),
                        SYNTAX_DIAGNOSTIC);
            }
            if (ruleName.equals(getFloatLiteralRuleName())) {
                return new SyntaxErrorMessage(TRANSLATOR.getMessage("error.expectedFloat"),
                        SYNTAX_DIAGNOSTIC);
            }
            if (ruleName.equals(getStringLiteralRuleName())) {
                return new SyntaxErrorMessage(TRANSLATOR.getMessage("error.expectedString"),
                        SYNTAX_DIAGNOSTIC);
            }
            if (ruleName.equals(getConstantRuleName())) {
                return new SyntaxErrorMessage(TRANSLATOR.getMessage("error.expectedConstant"),
                        SYNTAX_DIAGNOSTIC);
            }
            if (ruleName.equals(getVariableRuleName())) {
                return new SyntaxErrorMessage(TRANSLATOR.getMessage("error.expectedVariable"),
                        SYNTAX_DIAGNOSTIC);
            }
            final Optional<TerminalRule> terminalRule = tryFind(allTerminalRules(grammarAccess.getGrammar()),
                    rule -> ruleNames.getAntlrRuleName(rule).equals(expectedToken));
            if (terminalRule.isPresent()) {
                final String expectedKeyword = ((Keyword) terminalRule.get().getAlternatives()).getValue();
                return new SyntaxErrorMessage(TRANSLATOR.getMessage("error.mismatchedKeyword",
                        expectedKeyword), SYNTAX_DIAGNOSTIC);
            } else {
                return getDefaultSyntaxErrorMessage(context);
            }
        } catch (final Exception e) {
            return getDefaultSyntaxErrorMessage(context);
        }
    }

    private String getIntegerLiteralRuleName() {
        return grammarAccess.getT_INTEGER_LITERALRule().getName();
    }

    private String getFloatLiteralRuleName() {
        return grammarAccess.getT_DOUBLE_LITERALRule().getName();
    }

    private String getStringLiteralRuleName() {
        return grammarAccess.getT_STRING_LITERALRule().getName();
    }

    private String getConstantRuleName() {
        return grammarAccess.getT_CONSTANTRule().getName();
    }

    private String getVariableRuleName() {
        return grammarAccess.getT_VARIABLERule().getName();
    }
}
