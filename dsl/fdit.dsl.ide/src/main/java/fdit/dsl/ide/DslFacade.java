package fdit.dsl.ide;

import java.io.IOException;
import java.util.Collection;

public interface DslFacade {

    void initialize();

    void shutdown();

    void parse(final String attackScenario) throws IOException;

    Collection<SyntaxFault> getParseErrors();

    Collection<CompletionProposal> getProposals(final int caretOffset,
                                                final int selectionLength,
                                                final int limit);

    Collection<StylesPosition> getHighlightingStyles();
}
