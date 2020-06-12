package fdit.metamodel.alteration.action;

import fdit.metamodel.alteration.parameters.ActionParameter;
import fdit.metamodel.alteration.scope.Scope;

import java.util.Collection;

public abstract class Action {

    private final ActionType actionType;
    private final String name;
    private final String description;
    private final String target;
    private Scope scope;
    protected final Collection<ActionParameter> parameters;

    public Action(final ActionType actionType,
                  final String name,
                  final String description,
                  final String target,
                  final Scope scope,
                  final Collection<ActionParameter> parameters) {
        this.actionType = actionType;
        this.name = name;
        this.description = description;
        this.target = target;
        this.scope = scope;
        this.parameters = parameters;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getTarget() {
        return target;
    }

    public Scope getScope() {
        return scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }

    public Collection<ActionParameter> getParameters() {
        return parameters;
    }

    public abstract Action copy();

    public enum ActionType {
        ALTERATION,
        CREATION,
        DELETION,
        REPLAY,
        SATURATION,
        TIMESTAMP,
        TRAJECTORY_MODIFICATION
    }

    public interface ActionTypeSwitch<T> {

        T visitAlteration();

        T visitDeletion();

        T visitSaturation();

        T visitTimestamp();

        T visitReplay();

        T visitTrajectoryModification();

        T visitCreation();

        default T visitDefault() {
            throw new RuntimeException("Unknown scenario action type");
        }

        default T doSwitch(final ActionType type) {
            switch (type) {
                case ALTERATION:
                    return visitAlteration();
                case REPLAY:
                    return visitReplay();
                case CREATION:
                    return visitCreation();
                case DELETION:
                    return visitDeletion();
                case SATURATION:
                    return visitSaturation();
                case TIMESTAMP:
                    return visitTimestamp();
                case TRAJECTORY_MODIFICATION:
                    return visitTrajectoryModification();
                default:
                    return visitDefault();
            }
        }
    }

    public interface ActionVisitor<T> {

        T visitAlteration(final Alteration alteration);

        T visitCreation(final Creation creation);

        T visitDeletion(final Deletion deletion);

        T visitSaturation(final Saturation saturation);

        T visitDelay(final Delay delay);

        T visitReplay(final Replay replay);

        T visitTrajectoryModification(final TrajectoryModification trajectoryModification);

        default T visitDefault() {
            throw new RuntimeException("Unknown scenario action type");
        }

        default T accept(final Action action) {
            if (action instanceof Alteration) {
                return visitAlteration((Alteration) action);
            }
            if (action instanceof Replay) {
                return visitReplay((Replay) action);
            }
            if (action instanceof Deletion) {
                return visitDeletion((Deletion) action);
            }
            if (action instanceof Saturation) {
                return visitSaturation((Saturation) action);
            }
            if (action instanceof Delay) {
                return visitDelay((Delay) action);
            }
            if (action instanceof TrajectoryModification) {
                return visitTrajectoryModification((TrajectoryModification) action);
            }
            if (action instanceof Creation) {
                return visitCreation((Creation) action);
            }
            return visitDefault();
        }
    }
}