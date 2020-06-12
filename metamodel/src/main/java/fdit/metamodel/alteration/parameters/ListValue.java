package fdit.metamodel.alteration.parameters;

import java.util.List;

public class ListValue extends AlterationParameter {

    private final List<String> values;
    private final boolean isOffset;

    public ListValue(final Characteristic characteristic,
                     final List<String> values,
                     final boolean isOffset) {
        super(characteristic);
        this.values = values;
        this.isOffset = isOffset;
    }

    public ListValue(final Characteristic characteristic,
                     final List<String> values) {
        super(characteristic);
        this.values = values;
        isOffset = false;
    }

    public List<String> getValues() {
        return values;
    }

    public boolean isOffset() {
        return isOffset;
    }
}