package fdit.metamodel.alteration.action;

import fdit.metamodel.alteration.parameters.AlterationParameter;
import fdit.metamodel.alteration.parameters.RecordName;
import fdit.metamodel.alteration.scope.Scope;

import java.util.Collection;

import static com.google.common.collect.Lists.newArrayList;
import static fdit.metamodel.alteration.action.Action.ActionType.REPLAY;

public class Replay extends Action {

    private final RecordName recordName;
    private final Collection<AlterationParameter> replayParameters = newArrayList();

    public Replay(final String name,
                  final String description,
                  final String target,
                  final Scope scope,
                  final RecordName recordName,
                  final Collection<AlterationParameter> parameters) {
        super(REPLAY, name, description, target, scope, newArrayList(recordName));
        this.parameters.addAll(parameters);
        this.replayParameters.addAll(parameters);
        this.recordName = recordName;
    }

    public RecordName getRecordName() {
        return recordName;
    }

    public Collection<AlterationParameter> getReplayParameters() {
        return replayParameters;
    }

    @Override
    public Replay copy() {
        return new Replay(
                getName(),
                getDescription(),
                getTarget(),
                getScope(),
                getRecordName(),
                getReplayParameters());
    }
}