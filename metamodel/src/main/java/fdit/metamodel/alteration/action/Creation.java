package fdit.metamodel.alteration.action;

import fdit.metamodel.alteration.parameters.AlterationParameter;
import fdit.metamodel.alteration.parameters.Trajectory;
import fdit.metamodel.alteration.scope.Scope;

import java.util.Collection;

import static com.google.common.collect.Lists.newArrayList;
import static fdit.metamodel.alteration.action.Action.ActionType.CREATION;

public class Creation extends Action {

    private final Trajectory trajectory;
    private final Collection<AlterationParameter> creationParameters = newArrayList();

    public Creation(final String name,
                    final String description,
                    final String target,
                    final Scope scope,
                    final Trajectory trajectory,
                    final Collection<AlterationParameter> parameters) {
        super(CREATION, name, description, target, scope, newArrayList(trajectory));
        this.parameters.addAll(parameters);
        this.creationParameters.addAll(parameters);
        this.trajectory = trajectory;
    }

    public Trajectory getTrajectory() {
        return trajectory;
    }

    public Collection<AlterationParameter> getCreationParameters() {
        return creationParameters;
    }

    @Override
    public Creation copy() {
        return new Creation(
                getName(),
                getDescription(),
                getTarget(),
                getScope(),
                getTrajectory(),
                getCreationParameters());
    }
}