package fdit.gui.schemaEditor.schemaInterpretation.memory;

public abstract class Constant {
    protected String name;

    protected Constant(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}