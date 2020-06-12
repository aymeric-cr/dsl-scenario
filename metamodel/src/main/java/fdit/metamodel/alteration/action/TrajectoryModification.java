package fdit.metamodel.alteration.action;

import fdit.metamodel.alteration.parameters.Trajectory;
import fdit.metamodel.alteration.scope.Scope;

import static com.google.common.collect.Lists.newArrayList;
import static fdit.metamodel.alteration.action.Action.ActionType.TRAJECTORY_MODIFICATION;

public class TrajectoryModification extends Action {

    private final Trajectory trajectory;

    public TrajectoryModification(final String name,
                                  final String description,
                                  final String target,
                                  final Scope scope,
                                  final Trajectory trajectory) {
        super(TRAJECTORY_MODIFICATION, name, description, target, scope, newArrayList(trajectory));
        this.trajectory = trajectory;
    }

    public Trajectory getTrajectory() {
        return trajectory;
    }

    @Override
    public TrajectoryModification copy() {
        return new TrajectoryModification(getName(), getDescription(), getTarget(), getScope(), getTrajectory());
    }
}