package fdit.triggcondition.ide

import org.eclipse.xtext.ide.editor.contentassist.IdeContentProposalProvider
import org.eclipse.xtext.RuleCall
import org.eclipse.xtext.TerminalRule
import org.eclipse.xtext.Keyword
import org.eclipse.xtext.ide.editor.contentassist.ContentAssistContext
import org.eclipse.xtext.ide.editor.contentassist.IIdeContentProposalAcceptor

class TriggeringConditionContentProposalProvider extends IdeContentProposalProvider  {

    override _createProposals(RuleCall ruleCall, ContentAssistContext context, IIdeContentProposalAcceptor acceptor) {
        val rule = ruleCall.rule
        if(rule instanceof TerminalRule) {
            val alternatives = rule.getAlternatives()
            if(alternatives instanceof Keyword) {
                createProposals(alternatives, context, acceptor)
            }
        }
    }
}