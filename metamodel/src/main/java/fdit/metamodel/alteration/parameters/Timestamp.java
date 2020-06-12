package fdit.metamodel.alteration.parameters;

public class Timestamp implements ActionParameter {

    private final long value;
    private boolean offset;

    public Timestamp(final long value) {
        this.value = value;
        this.offset = false;
    }

    public Timestamp(final long value, final boolean offset) {
        this.value = value;
        this.offset = offset;
    }

    public long getValue() {
        return value;
    }

    public boolean isOffset() {
        return offset;
    }

    public void setOffset(boolean offset) {
        this.offset = offset;
    }
}