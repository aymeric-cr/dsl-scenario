package fdit.metamodel.alteration.parameters;

public class RangeValue extends AlterationParameter {

    private final String rangeMinContent;
    private final String rangeMaxContent;
    private final boolean isOffset;

    public RangeValue(final Characteristic characteristic,
                      final String rangeMinContent,
                      final String rangeMaxContent,
                      final boolean isOffset) {
        super(characteristic);
        this.rangeMinContent = rangeMinContent;
        this.rangeMaxContent = rangeMaxContent;
        this.isOffset = isOffset;
    }

    public RangeValue(final Characteristic characteristic,
                      final String rangeMinContent,
                      final String rangeMaxContent) {
        super(characteristic);
        this.rangeMinContent = rangeMinContent;
        this.rangeMaxContent = rangeMaxContent;
        isOffset = false;
    }

    public String getRangeMinContent() {
        return rangeMinContent;
    }

    public String getRangeMaxContent() {
        return rangeMaxContent;
    }

    public boolean isOffset() {
        return isOffset;
    }
}