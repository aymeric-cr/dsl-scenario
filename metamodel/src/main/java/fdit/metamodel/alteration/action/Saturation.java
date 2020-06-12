package fdit.metamodel.alteration.action;

import fdit.metamodel.alteration.parameters.ActionParameter;
import fdit.metamodel.alteration.parameters.AircraftNumber;
import fdit.metamodel.alteration.scope.Scope;

import static com.google.common.collect.Lists.newArrayList;
import static fdit.metamodel.alteration.action.Action.ActionType.SATURATION;

public class Saturation extends Action {

    private final AircraftNumber aircraftNumber;
    private final ActionParameter icaoParameter;

    public Saturation(
            final String name,
            final String description,
            final String target,
            final Scope scope,
            final AircraftNumber aircraftNumber,
            final ActionParameter icaoParameter) {
        super(SATURATION, name, description, target, scope, newArrayList(aircraftNumber, icaoParameter));
        this.aircraftNumber = aircraftNumber;
        this.icaoParameter = icaoParameter;
    }

    public AircraftNumber getAircraftNumber() {
        return aircraftNumber;
    }

    public ActionParameter getIcaoParameter() {
        return icaoParameter;
    }

    @Override
    public Saturation copy() {
        return new Saturation(
                getName(),
                getDescription(),
                getTarget(),
                getScope(),
                getAircraftNumber(),
                getIcaoParameter());
    }
}