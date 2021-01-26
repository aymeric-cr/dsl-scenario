package fdit.gui.schemaEditor.schemaInterpretation;

import fdit.dsl.attackScenario.*;
import fdit.dsl.attackScenario.util.AttackScenarioSwitch;
import fdit.dsl.ide.AttackScenarioFacade;
import fdit.gui.schemaEditor.schemaInterpretation.memory.Memory;
import fdit.ltlcondition.ide.LTLConditionFacade;
import fdit.metamodel.aircraft.Aircraft;
import fdit.metamodel.aircraft.TimeInterval;
import fdit.metamodel.alteration.AlterationSchema;
import fdit.metamodel.alteration.AlterationSpecification;
import fdit.metamodel.alteration.action.*;
import fdit.metamodel.alteration.parameters.*;
import fdit.metamodel.alteration.scope.Scope;
import fdit.metamodel.alteration.scope.TimeWindow;
import fdit.metamodel.coordinates.Coordinates;
import fdit.metamodel.filter.LTLFilter;
import fdit.metamodel.rap.RecognizedAirPicture;
import fdit.metamodel.recording.Recording;
import fdit.metamodel.schema.Schema;
import fdit.metamodel.trigger.ActionTrigger;
import fdit.triggcondition.ide.TriggeringConditionFacade;

import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.google.common.base.Throwables.throwIfUnchecked;
import static com.google.common.collect.Lists.newArrayList;
import static fdit.gui.application.FditManager.FDIT_MANAGER;
import static fdit.gui.schemaEditor.schemaInterpretation.AttackScenarioInterpretationUtils.getCharacteristicByString;
import static fdit.metamodel.aircraft.AircraftUtils.renderAircraftIds;
import static fdit.metamodel.alteration.parameters.Mode.DRIFT;
import static fdit.metamodel.alteration.parameters.Mode.NOISE;
import static fdit.metamodel.element.DirectoryUtils.*;
import static fdit.storage.alteration.AlterationSpecificationStorage.PARAMETER_FREQUENCY;
import static fdit.storage.alteration.AlterationSpecificationStorage.PARAMETER_NUMBER;
import static fdit.tools.stream.StreamUtils.*;
import static java.lang.Integer.parseInt;
import static java.lang.String.valueOf;
import static org.apache.commons.collections4.IterableUtils.first;
import static org.apache.commons.collections4.IterableUtils.get;

class SchemaSwitchInterpretation extends AttackScenarioSwitch<Object> {

    private final Memory memory;
    private final LTLConditionFacade filterFacade = LTLConditionFacade.get();
    private final TriggeringConditionFacade triggerFacade = TriggeringConditionFacade.get();
    private final Collection<Aircraft> targetedAircrafts = newArrayList();
    private final AttackScenarioFacade attackScenarioFacade;
    private Schema schema;
    private String currentBinaryOp = "";
    private TimeInterval timeInterval;

    SchemaSwitchInterpretation(final AttackScenarioFacade attackScenarioFacade)
            throws NoSuchAlgorithmException {
        this.attackScenarioFacade = attackScenarioFacade;
        filterFacade.initialize(FDIT_MANAGER.getRoot());
        triggerFacade.initialize(FDIT_MANAGER.getRoot());
        memory = new Memory();
    }

    public AlterationSpecification processInterpretation(final Schema schema) {
        memory.clear();
        this.schema = schema;
        timeInterval = new TimeInterval(0, schema.getRecording().getMaxRelativeDate());
        try {
            attackScenarioFacade.parse(schema.getContent());
        } catch (final Exception e) {
            throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
        return (AlterationSpecification) doSwitch(get(attackScenarioFacade.getAST(), 0));
    }

    @Override
    public Object caseASTScenario(final ASTScenario object) {
        final Collection<Action> scenarioActions = newArrayList();
        targetedAircrafts.clear();
        object.getInstructions().forEach(astInstruction -> {
            scenarioActions.addAll((Collection<Action>) doSwitch(astInstruction));
        });
        final AlterationSchema alterationSchema = new AlterationSchema(
                "Scenario_0",
                "Scenario_0",
                scenarioActions);
        final AlterationSpecification alterationSpecification =
                new AlterationSpecification(schema.getName(), alterationSchema);
        alterationSpecification.setFather(schema.getFather());
        return alterationSpecification;
    }


    private double getDoubleOffset(final ASTValue offset) {
        if (offset instanceof ASTLeftShift) {
            if (((ASTLeftShift) offset).getContent() instanceof ASTIntegerValue) {
                return 0.0 - (int) doSwitch(offset);
            } else {
                return -(double) doSwitch(offset);
            }
        } else if (offset instanceof ASTRightShift) {
            if (((ASTRightShift) offset).getContent() instanceof ASTIntegerValue) {
                return (int) doSwitch(offset) * 1.0;
            } else {
                return (double) doSwitch(offset);
            }
        } else if (offset instanceof ASTNumber) {
            if (offset instanceof ASTIntegerValue) {
                return ((ASTIntegerValue) offset).getContent() * 1.0;
            } else if (offset instanceof ASTDoubleValue) {
                return ((ASTDoubleValue) offset).getContent();
            }
        }
        return 0.0;
    }

    private int getIntegerOffset(final ASTValue offset) {
        if (offset instanceof ASTLeftShift) {
            return -(int) doSwitch(offset);
        } else if (offset instanceof ASTRightShift) {
            return (int) doSwitch(offset);
        } else if (offset instanceof ASTNumber) {
            if (offset instanceof ASTIntegerValue) {
                return ((ASTIntegerValue) offset).getContent();
            } else if (offset instanceof ASTDoubleValue) {
                return (int) ((ASTDoubleValue) offset).getContent();
            }
        }
        return 0;
    }

    @Override
    public Object caseASTParameters(final ASTParameters object) {
        final Collection<AlterationParameter> actionParameters = newArrayList();
        for (final ASTParameter parameter : object.getItems()) {
            actionParameters.add((AlterationParameter) doSwitch(parameter));
        }
        return actionParameters;
    }

    @Override
    public Object caseASTCreationParameters(final ASTCreationParameters object) {
        final Collection<AlterationParameter> actionParameters = newArrayList();
        for (final ASTCreationParameter parameter : object.getItems()) {
            actionParameters.add((AlterationParameter) doSwitch(parameter));
        }
        return actionParameters;
    }

    @Override
    public Object caseASTCreationParameter(final ASTCreationParameter object) {
        return getParameter(object.getName().getLiteral(), object.getValue(), false);
    }

    @Override
    public Object caseASTParamEdit(final ASTParamEdit object) {
        return getParameter(object.getName().getLiteral(), object.getValue(), false);
    }

    @Override
    public Object caseASTParamOffset(final ASTParamOffset object) {
        final Characteristic characteristic = AttackScenarioInterpretationUtils.getCharacteristicByString(object.getName().getLiteral());
        Object realValue = doSwitch(object.getValue());
        if (object.getOffset_op().equals("-=")) {
            if (realValue instanceof Integer) realValue = -((Integer) realValue);
            else if (realValue instanceof Double) realValue = -((Double) realValue);
        }
        return new Value(characteristic, valueOf(realValue), true);
    }

    @Override
    public Object caseASTParamNoise(final ASTParamNoise object) {
        final Characteristic characteristic = getCharacteristicByString(object.getName().getLiteral());
        final Object realValue = doSwitch(object.getValue());
        return new Value(characteristic, valueOf(realValue), NOISE);
    }

    @Override
    public Object caseASTParamDrift(final ASTParamDrift object) {
        final Characteristic characteristic = getCharacteristicByString(object.getName().getLiteral());
        Object realValue = doSwitch(object.getValue());
        if (object.getDrift_op().compareTo("--=") == 0) {
            if (realValue instanceof Integer) realValue = -((Integer) realValue);
            else if (realValue instanceof Double) realValue = -((Double) realValue);
        }
        return new Value(characteristic, valueOf(realValue), DRIFT);
    }

    private ActionParameter getParameter(final String characName, final ASTValue value, boolean isOffset) {
        final Characteristic characteristic = AttackScenarioInterpretationUtils.getCharacteristicByString(characName);
        return new Value(characteristic, valueOf(doSwitch(value)), isOffset);
    }

    @Override
    public Object caseASTHideParameters(final ASTHideParameters object) {
        final Collection<ActionParameter> actionParameters = newArrayList();
        for (final ASTHideParameter parameter : object.getItems()) {
            actionParameters.add((ActionParameter) doSwitch(parameter));
        }
        return actionParameters;
    }

    @Override
    public Object caseASTHideParameter(final ASTHideParameter object) {
        if (object.getName().getLiteral().compareToIgnoreCase(PARAMETER_FREQUENCY) == 0) {
            return new Frequency(parseInt(valueOf(doSwitch(object.getValue()))));
        }
        return null;
    }

    @Override
    public Object caseASTSaturationParameters(final ASTSaturationParameters object) {
        final Collection<ActionParameter> actionParameters = newArrayList();
        for (final ASTSaturationParameter parameter : object.getItems()) {
            actionParameters.add((ActionParameter) doSwitch(parameter));
        }
        return actionParameters;
    }

    @Override
    public Object caseASTSaturationParameter(final ASTSaturationParameter object) {
        if (object.getName().getLiteral().compareToIgnoreCase(PARAMETER_NUMBER) == 0) {
            return new AircraftNumber(parseInt(valueOf(doSwitch(object.getValue()))));
        } else {
            final Characteristic characteristic = AttackScenarioInterpretationUtils.getCharacteristicByString(object.getName().getLiteral());
            final ASTValue astValue = object.getValue();
            return new Value(characteristic, valueOf(doSwitch(astValue)), false);
        }
    }

    @Override
    public Object caseASTTimeScope(final ASTTimeScope object) {
        throw new RuntimeException();
    }

    @Override
    public Object caseASTAt(final ASTAt object) {
        final Recording recording = schema.getRecording();
        final long maxRelativeDate = recording.getMaxRelativeDate();
        timeInterval = new TimeInterval(((long) doSwitch(object.getTime())), maxRelativeDate);
        return new TimeWindow(timeInterval.getStart(), timeInterval.getEnd());
    }

    @Override
    public Object caseASTWindow(final ASTWindow object) {
        final Recording recording = schema.getRecording();
        final long start = (long) doSwitch(object.getStart());
        final long end;
        if (object.getEnd() == null) {
            end = recording.getMaxRelativeDate();
        } else {
            end = (long) doSwitch(object.getEnd());
        }
        timeInterval = new TimeInterval(start, end);
        return new TimeWindow(start, end);
    }

    @Override
    public Object caseASTAtFor(final ASTAtFor object) {
        final long start = (long) doSwitch(object.getTime());
        final long end = start + (long) doSwitch(object.getFor());
        timeInterval = new TimeInterval(start, end);
        return new TimeWindow(start, end);
    }

    @Override
    public Object caseASTTime(final ASTTime object) {
        if (object.getRealTime() instanceof ASTIntegerValue) {
            return ((Integer) doSwitch(object.getRealTime())).longValue() * 1000;
        }
        final Object result = doSwitch(object.getRealTime());
        if (result instanceof Double) {
            return ((Double) ((Double) result * 1000L)).longValue();
        } else {
            return ((Long) doSwitch(object.getRealTime())) * 1000;
        }
    }

    @Override
    public Object caseASTValue(final ASTValue object) {
        throw new RuntimeException();
    }

    @Override
    public Object caseASTAllPlaneFrom(final ASTAllPlaneFrom object) {
        final Optional<Recording> recording = gatherAllRecordings(FDIT_MANAGER.getRoot()).stream()
                .filter(f -> f.getName().equals(((ASTStringValue) object.getRecording()).getContent())).findFirst();
        if (recording.isPresent()) {
            recording.get().load();
            memory.setTargetedRecording(recording.get());
            if (object.getFilters() == null) {
                return recording.get().getAircrafts();
            }
            return filter(recording.get().getAircrafts(), (Predicate<Aircraft>) doSwitch(object.getFilters()));
        }
        return newArrayList();
    }

    @Override
    public Object caseASTDelay(final ASTDelay object) {
        final Collection<Action> scenarioActions = newArrayList();
        targetedAircrafts.addAll((Collection<Aircraft>) doSwitch(object.getTarget()));
        final Scope scope = (Scope) doSwitch(object.getTimeScope());
        scenarioActions.add(
                new Delay(
                        "Delay_0",
                        "Description_0",
                        renderAircraftIds(targetedAircrafts),
                        scope,
                        (Timestamp) doSwitch(object.getDelay())));
        return scenarioActions;
    }

    @Override
    public Object caseASTDelayParameter(final ASTDelayParameter object) {
        final long delay = (long) doSwitch(object.getDelay());
        return new Timestamp(delay);
    }

    @Override
    public Object caseASTAlter(final ASTAlter object) {
        final Collection<Action> scenarioActions = newArrayList();
        targetedAircrafts.addAll((Collection<Aircraft>) doSwitch(object.getTarget()));
        final Scope scope = (Scope) doSwitch(object.getTimeScope());
        final Collection<AlterationParameter> parameters = (Collection<AlterationParameter>) doSwitch(object.getParameters());
        if (object.getTrigger() == null) {
            scenarioActions.add(
                    new Alteration(
                            "Alteration_0",
                            "Description_0",
                            renderAircraftIds(targetedAircrafts),
                            scope,
                            parameters));
        } else {
            ((HashMap<Aircraft, Collection<TimeInterval>>) doSwitch(object.getTrigger())).forEach((key, value) -> {
                int i = 0;
                for (final TimeInterval interval : value) {
                    scenarioActions.add(
                            new Alteration(
                                    "Alteration_" + key.getCallSign() + '_' + i,
                                    "Description_" + key.getCallSign() + '_' + i,
                                    valueOf(key.getAircraftId()),
                                    new TimeWindow(interval.getStart(), interval.getEnd()),
                                    parameters));
                    i++;
                }
            });
        }
        return scenarioActions;
    }

    @Override
    public Object caseASTHide(final ASTHide object) {
        final Collection<Action> scenarioActions = newArrayList();
        targetedAircrafts.addAll((Collection<Aircraft>) doSwitch(object.getTarget()));
        final Scope scope = (Scope) doSwitch(object.getTimeScope());
        final Frequency frequency;
        if (object.getParameters() == null) {
            frequency = new Frequency(0);
        } else {
            final Collection<ActionParameter> parameters = (Collection<ActionParameter>) doSwitch(object.getParameters());
            frequency = tryFind(parameters, Frequency.class).orElse(new Frequency(0));
        }
        if (object.getTrigger() == null) {
            scenarioActions.add(
                    new Deletion(
                            "Deletion_0",
                            "Description_0",
                            renderAircraftIds(targetedAircrafts),
                            scope,
                            frequency));
        } else {
            ((HashMap<Aircraft, Collection<TimeInterval>>) doSwitch(object.getTrigger())).forEach((key, value) -> {
                int i = 0;
                for (final TimeInterval interval : value) {
                    scenarioActions.add(
                            new Deletion(
                                    "Deletion_" + key.getCallSign() + '_' + i,
                                    "Description_" + key.getCallSign() + '_' + i,
                                    valueOf(key.getAircraftId()),
                                    new TimeWindow(interval.getStart(), interval.getEnd()),
                                    frequency));
                    i++;
                }
            });
        }
        return scenarioActions;
    }

    @Override
    public Object caseASTSaturate(final ASTSaturate object) {
        final Collection<Action> scenarioActions = newArrayList();
        targetedAircrafts.addAll((Collection<Aircraft>) doSwitch(object.getTarget()));
        final Scope scope = (Scope) doSwitch(object.getTimeScope());
        final Collection<ActionParameter> parameters = (Collection<ActionParameter>) doSwitch(object.getParameters());
        final AircraftNumber aircraftNumber = find(parameters, AircraftNumber.class);
        final AlterationParameter icaoParameter = find(parameters, AlterationParameter.class);
        if (object.getTrigger() == null) {
            scenarioActions.add(
                    new Saturation(
                            "Saturation_0",
                            "Description_0",
                            renderAircraftIds(targetedAircrafts),
                            scope,
                            aircraftNumber,
                            icaoParameter));
        } else {
            ((HashMap<Aircraft, Collection<TimeInterval>>) doSwitch(object.getTrigger())).forEach((key, value) -> {
                int i = 0;
                for (final TimeInterval interval : value) {
                    scenarioActions.add(
                            new Saturation(
                                    "Saturation_" + key.getCallSign() + '_' + i,
                                    "Description_" + key.getCallSign() + '_' + i,
                                    valueOf(key.getAircraftId()),
                                    new TimeWindow(interval.getStart(), interval.getEnd()),
                                    aircraftNumber,
                                    icaoParameter));
                    i++;
                }
            });
        }
        return scenarioActions;
    }

    @Override
    public Object caseASTCreate(final ASTCreate object) {
        final Collection<Action> scenarioActions = newArrayList();
        final Scope scope = (Scope) doSwitch(object.getTimeScope());
        if (scope instanceof TimeWindow) {
            final Trajectory trajectory = new Trajectory((Collection<AircraftWayPoint>) doSwitch(object.getTrajectory()));
            final Collection<AlterationParameter> parameters = (Collection<AlterationParameter>) doSwitch(object.getParameters());
            scenarioActions.add(
                    new Creation(
                            "Creation_0",
                            "Description_0",
                            renderAircraftIds(targetedAircrafts),
                            scope,
                            trajectory,
                            parameters));
        } else {
            throw new RuntimeException();
        }
        return scenarioActions;
    }

    @Override
    public Object caseASTReplay(final ASTReplay object) {
        memory.setReplayAttack(true);
        final Collection<Action> scenarioActions = newArrayList();
        final Collection<Aircraft> aircrafts = (Collection<Aircraft>) doSwitch(object.getTarget());
        final Collection<AlterationParameter> parameters = newArrayList();
        targetedAircrafts.addAll(aircrafts);
        if (object.getParameters() != null) {
            parameters.addAll((Collection<AlterationParameter>) doSwitch(object.getParameters()));
        }
        final Scope scope = (Scope) doSwitch(object.getTimeScope());
        scenarioActions.add(
                new Replay(
                        "Replay_0",
                        "Description_0",
                        renderAircraftIds(targetedAircrafts),
                        scope,
                        new RecordName(memory.getTargetedRecording().getName()),
                        parameters));
        return scenarioActions;
    }


    @Override
    public Object caseASTTrajectory(final ASTTrajectory object) {
        final Collection<Action> scenarioActions = newArrayList();
        targetedAircrafts.addAll((Collection<Aircraft>) doSwitch(object.getTarget()));
        final Scope scope = (Scope) doSwitch(object.getTimeScope());
        if (object.getTrigger() == null) {
            final Trajectory trajectory = new Trajectory((Collection<AircraftWayPoint>) doSwitch(object.getTrajectory()));
            scenarioActions.add(
                    new TrajectoryModification(
                            "Alteration_0",
                            "Description_0",
                            renderAircraftIds(targetedAircrafts),
                            scope,
                            trajectory));
        } else {
            ((HashMap<Aircraft, Collection<TimeInterval>>) doSwitch(object.getTrigger())).forEach((key, value) -> {
                int i = 0;
                for (final TimeInterval interval : value.stream()
                        .filter(in -> in.getType() == TimeInterval.IntervalType.TRUE).collect(Collectors.toList())) {
                    this.timeInterval = interval;
                    final Trajectory trajectory = new Trajectory((Collection<AircraftWayPoint>) doSwitch(object.getTrajectory()));
                    // AVE: Si on descend dans le noeud trajectory avant d'avoir les intervals de temps issus de la trigger
                    //      le temps de passage des waypoints sera relatif au scope de l'altération et pas à l'interval de la trigger
                    //      Du coup j'ai redescendu le parcours du noeud trajectory et je l'effectue pour chaque portion de trigger
                    //      Risque d'explosion si +1000 triggers?!
                    scenarioActions.add(
                            new TrajectoryModification(
                                    "Alteration_" + key.getCallSign() + '_' + i,
                                    "Description_" + key.getCallSign() + '_' + i,
                                    valueOf(key.getAircraftId()),
                                    new TimeWindow(interval.getStart(), interval.getEnd()),
                                    trajectory));
                    i++;
                }
            });
        }
        return scenarioActions;
    }


    @Override
    public Object caseASTWayPoints(final ASTWayPoints object) {
        final Collection<AircraftWayPoint> waypoints = newArrayList();
        for (final ASTWayPoint waypoint : object.getWaypoints()) {
            waypoints.add((AircraftWayPoint) doSwitch(waypoint));
        }
        return waypoints;
    }

    @Override
    public Object caseASTWayPoint(final ASTWayPoint object) {
        return new AircraftWayPoint(
                new Coordinates(
                        getDoubleOffset(object.getLatitude()),
                        getDoubleOffset(object.getLongitude())),
                getIntegerOffset(object.getAltitude()),
                (long) doSwitch(object.getTime()),
                isAnOffset(object.getLatitude()),
                isAnOffset(object.getLongitude()),
                isAnOffset(object.getAltitude()));
    }

    @Override
    public Object caseASTPlane(final ASTPlane object) {
        final Collection<Aircraft> aircrafts = newArrayList();
        if (object.getFilters() == null) {
            aircrafts.add(get(schema.getRecording().getAircrafts(), 0));
        } else {
            final Object[] array = filter(schema.getRecording().getAircrafts(),
                    (Predicate<? super Aircraft>) doSwitch(object.getFilters())).toArray();
            if (array.length > 0) {
                aircrafts.add((Aircraft) array[0]);
            }
        }
        return aircrafts;
    }

    @Override
    public Object caseASTAllPlanes(final ASTAllPlanes object) {
        final Collection<Aircraft> aircrafts = schema.getRecording().getAircrafts();
        if (object.getFilters() == null) {
            return aircrafts;
        }
        return filter(aircrafts, (Predicate<? super Aircraft>) doSwitch(object.getFilters()));
    }

    @Override
    public Object caseASTPlaneFrom(final ASTPlaneFrom object) {
        final Optional<Recording> recording = gatherAllRecordings(FDIT_MANAGER.getRoot()).stream()
                .filter(f -> f.getName().equals(((ASTStringValue) object.getRecording()).getContent())).findFirst();
        if (recording.isPresent()) {
            recording.get().load();
            memory.setTargetedRecording(recording.get());
            if (object.getFilters() == null) {
                return newArrayList(first(recording.get().getAircrafts()));
            }
            final Collection<Aircraft> aircrafts = filter(recording.get().getAircrafts(),
                    ((Predicate<Aircraft>) doSwitch(object.getFilters())));
            if (aircrafts.size() > 0) {
                newArrayList(first(aircrafts));
            }
        }
        return newArrayList();
    }

    @Override
    public Object caseASTTrigger(final ASTTrigger object) {
        final Recording recording = schema.getRecording();
        final long maxRelativeDate = recording.getMaxRelativeDate();
        final Optional<ActionTrigger> trigger = gatherAllAlterationTriggers(FDIT_MANAGER.getRoot()).stream()
                .filter(f -> f.getName().equals(doSwitch(object.getTriggername()))).findFirst();
        if (trigger.isPresent()) {
            triggerFacade.parse(trigger.get().getContent());
            final RecognizedAirPicture rap = new RecognizedAirPicture();
            rap.addAircrafts(recording.getAircrafts());
            rap.addZones(gatherAllZones(FDIT_MANAGER.getRoot()));
            rap.setRelativeDuration(maxRelativeDate);
            return triggerFacade.getAircraftIntervals(rap, targetedAircrafts, FDIT_MANAGER.getRoot(), timeInterval, recording);
        }
        throw new RuntimeException("Unknown trigger");
    }

    @Override
    public Object caseASTFilters(final ASTFilters object) {
        final Recording recording = memory.isReplayAttack() ? memory.getTargetedRecording() : schema.getRecording();
        final RecognizedAirPicture rap = new RecognizedAirPicture();
        rap.addAircrafts(recording.getAircrafts());
        rap.addZones(gatherAllZones(FDIT_MANAGER.getRoot()));
        for (final ASTValue value : object.getFilters()) {
            assert (value instanceof ASTStringValue);
            final String filterName = (String) doSwitch(value);
            final Optional<LTLFilter> filter = gatherAllLTLFilters(FDIT_MANAGER.getRoot()).stream()
                    .filter(f -> f.getName().equals(filterName)).findFirst();
            if (filter.isPresent()) {
                filterFacade.parse(filter.get().getContent());
                rap.setAircrafts(filterFacade.filterAircraft(rap, recording));
            }
        }
        if (rap.getAircrafts().size() < recording.getAircrafts().size()) {
            final List<Integer> filtered = rap.getAircrafts().stream().map(Aircraft::getAircraftId).collect(
                    Collectors.toList());
            return (Predicate<Aircraft>) ac -> filtered.contains(ac.getAircraftId());
        } else {
            return (Predicate<Aircraft>) a -> true; // TODO: Display message that the filter does not exist!
        }
    }

    @Override
    public Object caseASTLeftShift(final ASTLeftShift object) {
        return doSwitch(object.getContent());
    }

    @Override
    public Object caseASTRightShift(final ASTRightShift object) {
        return doSwitch(object.getContent());
    }

    @Override
    public Object caseASTStringValue(final ASTStringValue object) {
        return object.getContent();
    }

    @Override
    public Object caseASTIntegerValue(final ASTIntegerValue object) {
        return object.getContent();
    }

    @Override
    public Object caseASTDoubleValue(final ASTDoubleValue object) {
        return object.getContent();
    }

    @Override
    public Object caseASTRecordingValue(final ASTRecordingValue object) {
        switch (object.getContent()) {
            case REC_DURATION:
                final long maxRelativeDate = schema.getRecording().getMaxRelativeDate();
                return (long) (object.getRatio() * maxRelativeDate) / 1000;
            case ALT_DURATION:
                return (timeInterval.getStart() / 1000) + (object.getRatio() * timeInterval.getDuration() / 1000);
            default:
                return null;
        }
    }

    public static boolean isAnOffset(final ASTValue numberOffset) {
        return !(numberOffset instanceof ASTNumber) && numberOffset instanceof ASTNumberOffset;
    }
}