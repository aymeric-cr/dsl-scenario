package fdit.metamodel.alteration.parameters;

public abstract class AlterationParameter implements ActionParameter {

    protected final Characteristic characteristic;

    protected AlterationParameter(final Characteristic characteristic) {
        this.characteristic = characteristic;
    }

    public Characteristic getCharacteristic() {
        return characteristic;
    }
}
