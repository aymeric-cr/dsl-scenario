package fdit.ltlcondition.ide

import org.eclipse.xtext.ide.editor.contentassist.IdeContentProposalProvider
import org.eclipse.xtext.RuleCall
import org.eclipse.xtext.TerminalRule
import org.eclipse.xtext.Keyword
import org.eclipse.xtext.ide.editor.contentassist.ContentAssistContext
import org.eclipse.xtext.ide.editor.contentassist.IIdeContentProposalAcceptor
import fdit.ltlcondition.lTLCondition.ReferencedArea
import fdit.metamodel.zone.Zone
import fdit.metamodel.element.DirectoryUtils
import org.eclipse.xtext.ide.editor.contentassist.ContentAssistEntry
import org.eclipse.xtext.Assignment
import org.eclipse.xtext.CrossReference
import org.eclipse.xtext.util.TextRegion

class LTLConditionContentProposalProvider extends IdeContentProposalProvider  {

    override dispatch void createProposals(Assignment assignment, ContentAssistContext context,
    IIdeContentProposalAcceptor acceptor) {
        val terminal = assignment.terminal
        if(!(context.currentModel instanceof ReferencedArea && terminal instanceof RuleCall
                && (terminal as RuleCall).rule.name.equals("T_STRING_LITERAL"))) {
            if(terminal instanceof CrossReference) {
                createProposals(terminal, context, acceptor)
            } else if(terminal instanceof RuleCall) {
                val rule = terminal.rule
                if(rule instanceof TerminalRule && context.prefix.empty) {
                    val proposal =
                        if(rule.name == 'STRING')
                            '"' + assignment.feature + '"'
                        else
                            assignment.feature
                    val entry = proposalCreator.createProposal(proposal, context) [
                        if(rule.name == 'STRING') {
                            editPositions += new TextRegion(context.offset + 1, proposal.length - 2)
                            kind = ContentAssistEntry.KIND_TEXT
                        } else {
                            editPositions += new TextRegion(context.offset, proposal.length)
                            kind = ContentAssistEntry.KIND_VALUE
                        }
                        description = rule.name
                    ]
                    acceptor.accept(entry, proposalPriorities.getDefaultPriority(entry))
                }
            }
        }
    }

    override dispatch void createProposals(RuleCall ruleCall, ContentAssistContext context, IIdeContentProposalAcceptor acceptor) {
        val rule = ruleCall.rule
        if(context.currentModel instanceof ReferencedArea && rule.name.equals("T_STRING_LITERAL")) {
            for(Zone zone : DirectoryUtils.gatherAllZones(LTLConditionFacade.get.root)) {
                val proposal = '"' + zone.name + "\"";
                var entry = proposalCreator.createProposal(proposal,context)
                if(entry !== null) {
                    entry.label = zone.name
                    //                    entry.prefix = "zone: "
                    entry.kind = ContentAssistEntry.KIND_REFERENCE
                    acceptor.accept(entry, proposalPriorities.getKeywordPriority(proposal, entry))
                }
            }

        }
        else if(rule instanceof TerminalRule) {
            val alternatives = rule.getAlternatives()
            if(alternatives instanceof Keyword) {
                createProposals(alternatives, context, acceptor)
            }
        } /*else if (rule ) {

        } */
    }
}