package fdit.dsl.xtext.standalone;

public class CompletionProposal {

    private final String prefix;
    private final String proposal;
    private final String label;
    private final int nextCaretPosition;

    public CompletionProposal(final String prefix,
                              final String proposal,
                              final String label,
                              final int nextCaretPosition) {
        this.prefix = prefix;
        this.proposal = proposal;
        this.label = label;
        this.nextCaretPosition = nextCaretPosition;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getProposal() {
        return proposal;
    }

    public String getLabel() {
        return label;
    }

    public int getNextCaretPosition() {
        return nextCaretPosition;
    }
}
