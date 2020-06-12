package fdit.ltlcondition.ide

class CompletionProposal {
    var String prefix
    var String proposal
    var String label
    var int nextCaretPosition

    new(String prefix, String proposal, String label, int nextCaretPosition) {
        this.prefix = prefix
        this.proposal = proposal
        this.label = label
        this.nextCaretPosition = nextCaretPosition
    }

    def String getPrefix() {
        prefix
    }

    def String getProposal() {
        proposal
    }

    def String getLabel() {
        label
    }

    def int getNextCaretPosition() {
        nextCaretPosition
    }
}