package fdit.triggcondition.ide

import org.eclipse.xtext.ide.editor.syntaxcoloring.ISemanticHighlightingCalculator
import org.eclipse.xtext.resource.XtextResource
import org.eclipse.xtext.ide.editor.syntaxcoloring.IHighlightedPositionAcceptor
import org.eclipse.xtext.util.CancelIndicator
import org.eclipse.xtext.RuleCall
import org.eclipse.xtext.AbstractRule
import java.util.Collection
import org.eclipse.xtext.Keyword
import org.eclipse.xtext.EnumLiteralDeclaration
import com.google.inject.Inject
import fdit.triggcondition.services.TriggeringConditionGrammarAccess
import static org.eclipse.xtext.GrammarUtil.allRules

class TriggeringConditionHighlightingCalculator implements ISemanticHighlightingCalculator {

    public static val CONSTANT_STYLE = "identifierStyle"
    public static val ENUM_STYLE = "enumStyle"
    public static val KEYWORD_STYLE = "keywordStyle"
    public static val STRING_STYLE = "stringStyle"
    public static val NUMBER_STYLE = "numberStyle"
    public static val ERROR_STYLE = "errorStyle"

    @Inject
    TriggeringConditionGrammarAccess grammarAccess

    override provideHighlightingFor(XtextResource resource, IHighlightedPositionAcceptor acceptor, CancelIndicator cancelIndicator) {
        for(node : resource.parseResult.rootNode.asTreeIterable) {
            val styles = com.google.common.collect.Lists.newArrayList
            if(node.syntaxErrorMessage !== null) {
                styles.add(ERROR_STYLE)
            }
            val grammarElement = node.grammarElement
            if(grammarElement instanceof RuleCall) {
                val rule = grammarElement.rule
                switch (rule) {
                    case isOnKeyword(allKeywords, rule):
                        styles.add(KEYWORD_STYLE)
                    case isStringRule(rule):
                        styles.add(STRING_STYLE)
                    case isNumberRule(rule):
                        styles.add(NUMBER_STYLE)
                    /*case isConstantRule(rule):
                          styles.add(CONSTANT_STYLE)*/
                }

            }
            if(grammarElement instanceof EnumLiteralDeclaration) {
                styles.add(ENUM_STYLE)
            }
            if(!styles.isNullOrEmpty) {
                val stylesArray = newArrayOfSize(styles.size)
                acceptor.addPosition(node.getOffset(), node.getLength(), styles.toArray(stylesArray))
            }
        }
    }

    /*    def isConstantRule(AbstractRule rule) {
            rule == grammarAccess.t_CON
        }*/

    def isOnKeyword(Collection<Keyword> allKeywords, AbstractRule rule) {
        rule.alternatives instanceof Keyword && allKeywords.contains(rule.alternatives)
    }

    def isStringRule(AbstractRule rule) {
        rule == grammarAccess.t_STRING_LITERALRule
    }

    def isNumberRule(AbstractRule rule) {
        rule == grammarAccess.t_INTEGER_LITERALRule ||
                rule == grammarAccess.t_DOUBLE_LITERALRule
    }

    def Collection<Keyword> getAllKeywords() {
        val keywords = com.google.common.collect.Lists.newArrayList()
        for(rule : allRules(grammarAccess.getGrammar())) {
            val iterator = rule.eAllContents()
            while(iterator.hasNext()) {
                val object = iterator.next()
                if(object instanceof Keyword) {
                    keywords.add(object)
                }
            }
        }
        return keywords
    }

}