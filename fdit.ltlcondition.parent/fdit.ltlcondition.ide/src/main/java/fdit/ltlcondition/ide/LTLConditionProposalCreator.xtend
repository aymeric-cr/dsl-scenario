package fdit.ltlcondition.ide

import org.eclipse.xtext.ide.editor.contentassist.ContentAssistContext
import org.eclipse.xtext.ide.editor.contentassist.ContentAssistEntry
import org.eclipse.xtext.ide.editor.contentassist.IdeContentProposalCreator
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1

class LTLConditionProposalCreator extends IdeContentProposalCreator {

    override createProposal(String proposal, String prefix, ContentAssistContext context, String kind, Procedure1<? super ContentAssistEntry> init) {
        val entry = super.createProposal(proposal, prefix, context, kind, init)
        if(entry !== null) {
            entry.proposal = proposal + ' '
            entry.label = proposal
            entry.escapePosition = context.replaceRegion.offset + proposal.length + 1
        }
        return entry
    }

}