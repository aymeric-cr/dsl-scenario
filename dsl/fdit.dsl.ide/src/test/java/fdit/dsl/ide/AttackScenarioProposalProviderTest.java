package fdit.dsl.ide;

import org.junit.Test;
import java.io.IOException;
import java.util.Collection;
import java.util.function.Predicate;

import static fdit.testTools.PredicateAssert.assertThat;
import static fdit.testTools.predicate.CollectionPredicate.containsOnly;

public class AttackScenarioProposalProviderTest {

    private static Collection<CompletionProposal> computeProposals(final DslFacade dslFacade,
                                                                   final String text) throws IOException {
        dslFacade.parse(text);
        final int caretOffset = text.length();
        return dslFacade.getProposals(caretOffset, 0, 50);
    }
    private static Predicate<CompletionProposal> aProposal(final String expected) {
        return completionProposal -> completionProposal.getLabel().equals(expected);
    }

    @Test
    public void proposeToCompleteTerminals() throws IOException {
        final DslFacade dslFacade = new AttackScenarioFacade();
        dslFacade.initialize();
        assertThat(computeProposals(dslFacade, "a"), containsOnly(
                aProposal("alter"),
                aProposal("alter_speed")));
        assertThat(computeProposals(dslFacade, "h"), containsOnly(aProposal("hide")));
        assertThat(computeProposals(dslFacade, "c"), containsOnly(aProposal("create")));
        dslFacade.shutdown();
    }
}