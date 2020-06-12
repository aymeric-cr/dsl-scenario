package fdit.ltlcondition.ide

import com.google.inject.Inject
import org.eclipse.xtext.ide.editor.contentassist.ContentAssistContext
import org.eclipse.xtext.ide.editor.contentassist.antlr.ContentAssistContextFactory
import org.eclipse.xtext.ide.editor.contentassist.IdeContentProposalProvider
import java.util.concurrent.ExecutorService
import java.util.Collection
import org.eclipse.xtext.resource.XtextResource
import org.eclipse.xtext.util.ITextRegion
import org.eclipse.xtext.util.Pair
import java.util.Arrays
import org.eclipse.xtext.ide.editor.contentassist.ContentAssistEntry
import java.util.HashSet
import org.eclipse.xtext.ide.editor.contentassist.IIdeContentProposalAcceptor
import static java.util.Collections.EMPTY_LIST
import static org.eclipse.xtext.util.Tuples.create
import static java.util.concurrent.Executors.newCachedThreadPool
import static fdit.tools.stream.StreamUtils.mapping

class ContentAssistService {

    static val ContentAssistContext[] NO_CONTENT_ASSIST_CONTEXT = newArrayOfSize(0)

    @Inject ContentAssistContextFactory contextFactoryProvider

    @Inject IdeContentProposalProvider proposalProvider

    val ExecutorService threadPool = newCachedThreadPool()

    def void shutdown() {
        threadPool.shutdown()
    }

    def Collection<CompletionProposal> createProposals(XtextResource resource,
    String text,
    ITextRegion selection,
    int caretOffset,
    int limit) {
        contextFactoryProvider.setPool(threadPool)
        var ContentAssistContext[] contexts = getContexts(resource, text, selection, caretOffset)
        createProposals(Arrays.asList(contexts), limit)
    }

    def ContentAssistContext[] getContexts(XtextResource resource,
    String text,
    ITextRegion selection,
    int caretOffset) {
        if(caretOffset > text.length) {
            return NO_CONTENT_ASSIST_CONTEXT
        }
        contextFactoryProvider.create(text, selection, caretOffset, resource)
    }

    private def Collection<CompletionProposal> createProposals(Collection<ContentAssistContext> contexts,
    int proposalsLimit) {
        if(contexts.empty) {
            return EMPTY_LIST
        }
        val HashSet<Pair<Integer, ContentAssistEntry>> proposals = newHashSet
        var  IIdeContentProposalAcceptor acceptor = new IIdeContentProposalAcceptor() {
            override accept(ContentAssistEntry entry, int priority) {
                if(entry === null) {
                    return
                }
                if(entry.getProposal() === null) {
                    throw new IllegalArgumentException("proposal must not be null.")
                }
                proposals.add(create(priority, entry))
            }

            override canAcceptMoreProposals() {
                return proposals.size() < proposalsLimit
            }
        };
        proposalProvider.createProposals(contexts, acceptor)
        return toProposals(proposals);
    }

    private static def Collection<CompletionProposal> toProposals(Iterable<Pair<Integer, ContentAssistEntry>> proposals) {
        return mapping(proposals, [ proposal |
            var entry = proposal.getSecond()
            new CompletionProposal(entry.getPrefix(), entry.getProposal(), entry.getLabel(),
            entry.getEscapePosition())
        ])
    }
}