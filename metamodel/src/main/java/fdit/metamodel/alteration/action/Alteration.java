package fdit.metamodel.alteration.action;

import fdit.metamodel.alteration.parameters.AlterationParameter;
import fdit.metamodel.alteration.scope.Scope;

import java.util.Collection;

import static com.google.common.collect.Lists.newArrayList;
import static fdit.metamodel.alteration.action.Action.ActionType.ALTERATION;

public class Alteration extends Action {

    private final Collection<AlterationParameter> alterationParameters = newArrayList();

    public Alteration(final String name,
                      final String description,
                      final String target,
                      final Scope scope,
                      final Collection<AlterationParameter> parameters) {
        super(ALTERATION, name, description, target, scope, newArrayList(parameters));
        alterationParameters.addAll(parameters);
    }

    public Collection<AlterationParameter> getAlterationParameters() {
        return alterationParameters;
    }

    @Override
    public Alteration copy() {
        return new Alteration(getName(), getDescription(), getTarget(), getScope(), getAlterationParameters());
    }
}