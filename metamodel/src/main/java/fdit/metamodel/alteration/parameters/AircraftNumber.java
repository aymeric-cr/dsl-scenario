package fdit.metamodel.alteration.parameters;

public class AircraftNumber implements ActionParameter {

    private final int value;

    public AircraftNumber(final int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}