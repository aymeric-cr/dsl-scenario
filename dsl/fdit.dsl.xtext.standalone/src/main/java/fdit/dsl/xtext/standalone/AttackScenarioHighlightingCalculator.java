package fdit.dsl.xtext.standalone;

import com.google.inject.Inject;
import fdit.dsl.services.AttackScenarioGrammarGrammarAccess;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.*;
import org.eclipse.xtext.ide.editor.syntaxcoloring.IHighlightedPositionAcceptor;
import org.eclipse.xtext.ide.editor.syntaxcoloring.ISemanticHighlightingCalculator;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.util.CancelIndicator;

import java.util.Collection;

import static com.google.common.collect.Lists.newArrayList;
import static fdit.dsl.xtext.standalone.HighlightingStyles.*;
import static org.eclipse.xtext.GrammarUtil.allRules;

class AttackScenarioHighlightingCalculator implements ISemanticHighlightingCalculator {

    @Inject
    private AttackScenarioGrammarGrammarAccess grammarAccess;

    private static boolean isOnKeyword(final Collection<Keyword> allKeywords, final AbstractRule rule) {
        final AbstractElement alternatives = rule.getAlternatives();
        return alternatives instanceof Keyword && allKeywords.contains(alternatives);
    }

    @Override
    public void provideHighlightingFor(final XtextResource resource,
                                       final IHighlightedPositionAcceptor acceptor,
                                       final CancelIndicator cancelIndicator) {
        final Collection<Keyword> allKeywords = getAllKeywords();
        for (final INode node : resource.getParseResult().getRootNode().getAsTreeIterable()) {
            final Collection<String> styles = newArrayList();
            if (node.getSyntaxErrorMessage() != null) {
                styles.add(ERROR_STYLE);
            }
            final EObject grammarElement = node.getGrammarElement();
            if (grammarElement instanceof RuleCall) {
                final AbstractRule rule = ((RuleCall) grammarElement).getRule();
                if (isOnKeyword(allKeywords, rule)) {
                    styles.add(KEYWORD_STYLE);
                }
                if (isStringRule(rule)) {
                    styles.add(STRING_STYLE);
                }
                if (isNumberRule(rule)) {
                    styles.add(NUMBER_STYLE);
                }
                if (isConstantRule(rule)) {
                    styles.add(CONSTANT_STYLE);
                }
            }
            if (grammarElement instanceof EnumLiteralDeclaration) {
                styles.add(ENUM_STYLE);
            }
            if (!styles.isEmpty()) {
                final String[] stylesArray = new String[styles.size()];
                acceptor.addPosition(node.getOffset(), node.getLength(), styles.toArray(stylesArray));
            }
        }
    }

    private boolean isConstantRule(final AbstractRule rule) {
        return rule == grammarAccess.getT_CONSTANTRule();
    }

    private boolean isStringRule(final AbstractRule rule) {
        return rule == grammarAccess.getT_STRING_LITERALRule();
    }

    private boolean isNumberRule(final AbstractRule rule) {
        return rule == grammarAccess.getT_INTEGER_LITERALRule() ||
                rule == grammarAccess.getT_DOUBLE_LITERALRule();
    }

    private Collection<Keyword> getAllKeywords() {
        final Collection<Keyword> keywords = newArrayList();
        for (final AbstractRule rule : allRules(grammarAccess.getGrammar())) {
            final TreeIterator<EObject> iterator = rule.eAllContents();
            while (iterator.hasNext()) {
                final EObject object = iterator.next();
                if (object instanceof Keyword) {
                    keywords.add((Keyword) object);
                }
            }
        }
        return keywords;
    }
}
