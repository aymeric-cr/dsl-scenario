package fdit.metamodel.alteration.parameters;

import static java.lang.Math.max;

public class Frequency implements ActionParameter {

    private final int value;

    public Frequency(final int value) {
        this.value = max(value, 1);
    }

    public int getValue() {
        return value;
    }
}
