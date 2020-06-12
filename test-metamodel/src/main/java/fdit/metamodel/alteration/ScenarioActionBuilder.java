package fdit.metamodel.alteration;

import fdit.metamodel.alteration.action.*;
import fdit.metamodel.alteration.action.ScenarioAction.ActionType;
import fdit.metamodel.alteration.parameters.*;
import fdit.metamodel.alteration.scope.Scope;

import java.util.Collection;

import static com.google.common.collect.Lists.newArrayList;
import static fdit.metamodel.alteration.action.ScenarioAction.ActionType.*;
import static fdit.tools.stream.StreamUtils.filter;
import static java.util.Arrays.asList;

public class ScenarioActionBuilder {

    private final Collection<ActionParameter> parameters = newArrayList();
    private ActionType actionType;
    private String name;
    private String description;
    private String target;
    private Scope scope;
    private Trajectory trajectory;
    private Timestamp timestamp;
    private RecordName recordName;
    private ActionParameter icaoParameter;
    private AircraftNumber aircraftNumber;

    public ScenarioAction build() {

        if (actionType == ALTERATION) {
            return new Alteration(
                    name,
                    description,
                    target,
                    scope,
                    filter(parameters, AlterationParameter.class));
        }
        if (actionType == SATURATION) {
            return new Saturation(
                    name,
                    description,
                    target,
                    scope,
                    aircraftNumber,
                    icaoParameter);
        }
        if (actionType == DELETION) {
            return new Deletion(
                    name,
                    description,
                    target,
                    scope);
        }
        if (actionType == REPLAY) {
            return new Replay(
                    name,
                    description,
                    target,
                    scope,
                    recordName,
                    filter(parameters, AlterationParameter.class));
        }
        if (actionType == TIMESTAMP) {
            return new Delay(
                    name,
                    description,
                    target,
                    scope,
                    timestamp);
        }
        if (actionType == TRAJECTORY_MODIFICATION) {
            return new TrajectoryModification(
                    name,
                    description,
                    target,
                    scope,
                    trajectory);
        }
        if (actionType == CREATION) {
            return new Creation(
                    name,
                    description,
                    target,
                    scope,
                    trajectory,
                    filter(parameters, AlterationParameter.class));
        }
        return null;
    }

    public ScenarioActionBuilder withActionType(final ActionType actionType) {
        this.actionType = actionType;
        return this;
    }

    public ScenarioActionBuilder withName(final String name) {
        this.name = name;
        return this;
    }

    public ScenarioActionBuilder withDescription(final String description) {
        this.description = description;
        return this;
    }

    public ScenarioActionBuilder withTimestamp(final Timestamp timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public ScenarioActionBuilder withRecordName(final RecordName recordName) {
        this.recordName = recordName;
        return this;
    }

    public ScenarioActionBuilder withIcaoParameter(final ActionParameter icaoParameter) {
        this.icaoParameter = icaoParameter;
        return this;
    }

    public ScenarioActionBuilder withAircraftNumber(final AircraftNumber aircraftNumber) {
        this.aircraftNumber = aircraftNumber;
        return this;
    }

    public ScenarioActionBuilder withTarget(final String target) {
        this.target = target;
        return this;
    }

    public ScenarioActionBuilder withScope(final Scope scope) {
        this.scope = scope;
        return this;
    }

    public ScenarioActionBuilder addParameter(final ActionParameter parameter) {
        parameters.add(parameter);
        return this;
    }

    public ScenarioActionBuilder addParameters(final ActionParameter... parameters) {
        this.parameters.addAll(asList(parameters));
        return this;
    }

    public ScenarioActionBuilder addWaypoints(final AircraftWayPoint... wayPoints) {
        this.trajectory = new Trajectory(asList(wayPoints));
        return this;
    }
}
