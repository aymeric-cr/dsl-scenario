package fdit.metamodel.alteration.parameters;

import static fdit.metamodel.alteration.parameters.Mode.OFFSET;
import static fdit.metamodel.alteration.parameters.Mode.SIMPLE;

public class Value extends AlterationParameter {

    private final String content;
    private boolean isOffset;
    private Mode mode = SIMPLE;

    public Value(final Characteristic characteristic, final String content) {
        this(characteristic, content, false);
    }

    public Value(final Characteristic characteristic,
                 final String content,
                 final boolean isOffset) {
        super(characteristic);
        this.content = content;
        this.isOffset = isOffset;
        if (this.isOffset) {
            mode = OFFSET;
        }
    }

    public Value(final Characteristic characteristic,
                 final String content,
                 final Mode mode) {
        super(characteristic);
        this.content = content;
        this.mode = mode;
        isOffset = mode == OFFSET;
    }

    public String getContent() {
        return content;
    }

    public boolean isOffset() {
        return isOffset;
    }

    public void setOffset(final boolean offset) {
        isOffset = offset;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }
}