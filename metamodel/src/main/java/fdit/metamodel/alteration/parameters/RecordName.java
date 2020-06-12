package fdit.metamodel.alteration.parameters;

public class RecordName implements ActionParameter {

    private final String name;

    public RecordName(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}