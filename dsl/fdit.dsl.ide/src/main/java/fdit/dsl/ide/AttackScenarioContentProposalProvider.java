package fdit.dsl.ide;

import org.eclipse.xtext.*;
import org.eclipse.xtext.ide.editor.contentassist.ContentAssistContext;
import org.eclipse.xtext.ide.editor.contentassist.IIdeContentProposalAcceptor;
import org.eclipse.xtext.ide.editor.contentassist.IdeContentProposalProvider;

class AttackScenarioContentProposalProvider extends IdeContentProposalProvider {

    @Override
    protected void _createProposals(final RuleCall ruleCall,
                                    final ContentAssistContext context,
                                    final IIdeContentProposalAcceptor acceptor) {
        final AbstractRule rule = ruleCall.getRule();
        if (rule instanceof TerminalRule) {
            final AbstractElement alternatives = rule.getAlternatives();
            if (alternatives instanceof Keyword) {
                _createProposals((Keyword) alternatives, context, acceptor);
            }
        }
    }

    @Override
    protected void _createProposals(final Assignment assignment,
                                    final ContentAssistContext context,
                                    final IIdeContentProposalAcceptor acceptor) {
        // do nothing
    }
}
