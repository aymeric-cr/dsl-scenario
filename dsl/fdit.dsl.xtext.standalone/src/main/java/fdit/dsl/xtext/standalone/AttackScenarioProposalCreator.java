package fdit.dsl.xtext.standalone;

import org.eclipse.xtext.ide.editor.contentassist.ContentAssistContext;
import org.eclipse.xtext.ide.editor.contentassist.ContentAssistEntry;
import org.eclipse.xtext.ide.editor.contentassist.IdeContentProposalCreator;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;

public class AttackScenarioProposalCreator extends IdeContentProposalCreator {

    @Override
    public ContentAssistEntry createProposal(final String proposal,
                                             final String prefix,
                                             final ContentAssistContext context,
                                             final String kind,
                                             final Procedure1<? super ContentAssistEntry> init) {
        final ContentAssistEntry entry = super.createProposal(proposal, prefix, context, kind, init);
        if (entry != null) {
            entry.setProposal(proposal + ' ');
            entry.setLabel(proposal);
            entry.setEscapePosition(context.getReplaceRegion().getOffset() + proposal.length() + 1);
        }
        return entry;
    }
}