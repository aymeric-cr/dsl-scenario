package fdit.triggcondition.ide

import com.google.inject.Inject
import fdit.tools.i18n.MessageTranslator
import org.antlr.runtime.MismatchedTokenException
import org.antlr.runtime.RecognitionException
import org.antlr.runtime.Token
import org.eclipse.xtext.Keyword
import org.eclipse.xtext.TerminalRule
import org.eclipse.xtext.nodemodel.SyntaxErrorMessage
import org.eclipse.xtext.parser.antlr.SyntaxErrorMessageProvider
import org.eclipse.xtext.xtext.RuleNames

import java.util.Optional

import static fdit.tools.i18n.MessageTranslator.createMessageTranslator
import static fdit.tools.stream.StreamUtils.tryFind
import static org.eclipse.xtext.GrammarUtil.allTerminalRules
import static org.eclipse.xtext.diagnostics.Diagnostic.SYNTAX_DIAGNOSTIC
import static org.eclipse.xtext.xtext.RuleNames.getRuleNames
import fdit.triggcondition.services.TriggeringConditionGrammarAccess

class TriggeringConditionErrorMessageProvider extends SyntaxErrorMessageProvider {

    static val MessageTranslator TRANSLATOR = createMessageTranslator(TriggeringConditionErrorMessageProvider)

    @Inject
    var TriggeringConditionGrammarAccess grammarAccess

    override getSyntaxErrorMessage(IParserErrorContext context) {
        val RecognitionException recognitionException = context.recognitionException
        if(recognitionException === null) {
            return new SyntaxErrorMessage(TRANSLATOR.getMessage("error.unexpectedToken_0"), SYNTAX_DIAGNOSTIC)
        }
        if(recognitionException instanceof MismatchedTokenException) {
            return getMismatchedErrorMessage(recognitionException, context)
        }
        context.getDefaultSyntaxErrorMessage
    }

    def getDefaultSyntaxErrorMessage(IParserErrorContext context) {
        val Token token = context.recognitionException.token
        if(token.type == Token.EOF) {
            new SyntaxErrorMessage(TRANSLATOR.getMessage("error.unexpectedEOF"),
            SYNTAX_DIAGNOSTIC)
        } else new SyntaxErrorMessage(TRANSLATOR.getMessage("error.unexpectedToken_1",token.text),SYNTAX_DIAGNOSTIC)
    }

    def getMismatchedErrorMessage(MismatchedTokenException mismatchedException,IParserErrorContext context) {
        if(mismatchedException.expecting == Token.EOF) {
            return context.getDefaultSyntaxErrorMessage
        } else {
            try {
                val String expectedToken = context.tokenNames.get(mismatchedException.expecting)
                val RuleNames ruleNames = getRuleNames(grammarAccess.grammar, true)
                val String ruleName = ruleNames.getRuleByAntlrName(expectedToken).name

                switch (ruleName) {
                    case equals(getIntegerLiteralRuleName()) :
                        return new SyntaxErrorMessage(TRANSLATOR.getMessage("error.expectedInteger"),
                        SYNTAX_DIAGNOSTIC)
                    case equals(getDoubleLiteralRuleName()) :
                        return new SyntaxErrorMessage(TRANSLATOR.getMessage("error.expectedFloat"),
                        SYNTAX_DIAGNOSTIC)
                    case equals(getStringLiteralRuleName()) :
                        return new SyntaxErrorMessage(TRANSLATOR.getMessage("error.expectedString"),
                        SYNTAX_DIAGNOSTIC)
                }

                val Optional<TerminalRule> terminalRule = tryFind(allTerminalRules(grammarAccess.grammar),
                [ TerminalRule rule | ruleNames.getAntlrRuleName(rule).equals(expectedToken)])
                if(terminalRule.isPresent) {
                    val String expectedKeyword = (terminalRule.get.alternatives as Keyword).value
                    return new SyntaxErrorMessage(TRANSLATOR.getMessage("error.mismatchedKeyword",
                    expectedKeyword), SYNTAX_DIAGNOSTIC)
                } else return context.getDefaultSyntaxErrorMessage
            } catch(Exception e) {
                return context.getDefaultSyntaxErrorMessage
            }
        }
    }

    def getIntegerLiteralRuleName() {
        grammarAccess.t_INTEGER_LITERALRule.name
    }

    def getDoubleLiteralRuleName() {
        grammarAccess.t_DOUBLE_LITERALRule.name
    }

    def getStringLiteralRuleName() {
        grammarAccess.t_STRING_LITERALRule.name
    }
}