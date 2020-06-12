package fdit.metamodel.alteration.action;

import fdit.metamodel.alteration.parameters.Timestamp;
import fdit.metamodel.alteration.scope.Scope;

import static com.google.common.collect.Lists.newArrayList;
import static fdit.metamodel.alteration.action.Action.ActionType.TIMESTAMP;

public class Delay extends Action {

    private final Timestamp timestamp;

    public Delay(final String name,
                 final String description,
                 final String target,
                 final Scope scope,
                 final Timestamp timestamp) {
        super(TIMESTAMP, name, description, target, scope, newArrayList(timestamp));
        this.timestamp = timestamp;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    @Override
    public Delay copy() {
        return new Delay(getName(), getDescription(), getTarget(), getScope(), getTimestamp());
    }
}