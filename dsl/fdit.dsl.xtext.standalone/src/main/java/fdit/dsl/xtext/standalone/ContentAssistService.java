package fdit.dsl.xtext.standalone;

import com.google.inject.Inject;
import org.eclipse.xtext.ide.editor.contentassist.ContentAssistContext;
import org.eclipse.xtext.ide.editor.contentassist.ContentAssistEntry;
import org.eclipse.xtext.ide.editor.contentassist.IIdeContentProposalAcceptor;
import org.eclipse.xtext.ide.editor.contentassist.IdeContentProposalProvider;
import org.eclipse.xtext.ide.editor.contentassist.antlr.ContentAssistContextFactory;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.util.ITextRegion;
import org.eclipse.xtext.util.Pair;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;

import static com.google.common.collect.Sets.newHashSet;
import static fdit.tools.stream.StreamUtils.mapping;
import static java.util.Collections.EMPTY_LIST;
import static java.util.concurrent.Executors.newCachedThreadPool;
import static org.eclipse.xtext.util.Tuples.create;

class ContentAssistService {

    private static final ContentAssistContext[] NO_CONTENT_ASSIST_CONTEXT = new ContentAssistContext[0];
    private final ExecutorService threadPool = newCachedThreadPool();
    @Inject
    private ContentAssistContextFactory contextFactoryProvider;
    @Inject
    private IdeContentProposalProvider proposalProvider;

    private static Collection<CompletionProposal> toProposals(final Iterable<Pair<Integer, ContentAssistEntry>> proposals) {
        return mapping(proposals, proposal -> {
            final ContentAssistEntry entry = proposal.getSecond();
            return new CompletionProposal(entry.getPrefix(), entry.getProposal(), entry.getLabel(),
                    entry.getEscapePosition());
        });
    }

    void shutdown() {
        threadPool.shutdown();
    }

    Collection<CompletionProposal> createProposals(final XtextResource resource,
                                                   final String text,
                                                   final ITextRegion selection,
                                                   final int caretOffset,
                                                   final int limit) {
        contextFactoryProvider.setPool(threadPool);
        final ContentAssistContext[] contexts = getContexts(resource, text, selection, caretOffset);
        return createProposals(Arrays.asList(contexts), limit);
    }

    public ContentAssistContext[] getContexts(final XtextResource resource,
                                              final String text,
                                              final ITextRegion selection,
                                              final int caretOffset) {
        if (caretOffset > text.length()) {
            return NO_CONTENT_ASSIST_CONTEXT;
        }
        final ContentAssistContextFactory contextFactory = contextFactoryProvider;
        return contextFactory.create(text, selection, caretOffset, resource);
    }

    private Collection<CompletionProposal> createProposals(final Collection<ContentAssistContext> contexts,
                                                           final int proposalsLimit) {
        if (contexts.isEmpty()) {
            return EMPTY_LIST;
        }
        final HashSet<Pair<Integer, ContentAssistEntry>> proposals = newHashSet();
        final IIdeContentProposalAcceptor acceptor = new IIdeContentProposalAcceptor() {
            @Override
            public void accept(final ContentAssistEntry entry, final int priority) {
                if (entry == null) {
                    return;
                }
                if (entry.getProposal() == null) {
                    throw new IllegalArgumentException("proposal must not be null.");
                }
                proposals.add(create(priority, entry));
            }

            @Override
            public boolean canAcceptMoreProposals() {
                return proposals.size() < proposalsLimit;
            }
        };
        proposalProvider.createProposals(contexts, acceptor);
        return toProposals(proposals);
    }
}
