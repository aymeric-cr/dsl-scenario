package fdit.metamodel.alteration.action;

import fdit.metamodel.alteration.parameters.Frequency;
import fdit.metamodel.alteration.scope.Scope;

import static com.google.common.collect.Lists.newArrayList;
import static fdit.metamodel.alteration.action.Action.ActionType.DELETION;

public class Deletion extends Action {

    final Frequency frequency;

    public Deletion(final String name,
                    final String description,
                    final String target,
                    final Scope scope) {
        this(name, description, target, scope, new Frequency(1));
    }

    public Deletion(final String name,
                    final String description,
                    final String target,
                    final Scope scope,
                    final Frequency frequency) {
        super(DELETION, name, description, target, scope, newArrayList(frequency));
        this.frequency = frequency;
    }

    @Override
    public Deletion copy() {
        return new Deletion(getName(), getDescription(), getTarget(), getScope());
    }

    public Frequency getFrequency() {
        return frequency;
    }
}