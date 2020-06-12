package fdit.metamodel.alteration;

import fdit.metamodel.element.FditElement;

public class AlterationSpecification extends FditElement {

    private final AlterationSchema alterationSchema;

    public AlterationSpecification(final String name,
                                   final AlterationSchema alterationSchema) {
        super(name);
        this.alterationSchema = alterationSchema;
    }

    public AlterationSchema getAlterationSchema() {
        return alterationSchema;
    }
}