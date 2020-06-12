package fdit.gui.schemaEditor.schemaInterpretation;

import fdit.dsl.attackScenario.*;
import fdit.dsl.attackScenario.util.AttackScenarioSwitch;
import fdit.dsl.xtext.standalone.AttackScenarioDslFacade;
import fdit.gui.schemaEditor.schemaInterpretation.memory.Constant;
import fdit.gui.schemaEditor.schemaInterpretation.memory.ListConstant;
import fdit.gui.schemaEditor.schemaInterpretation.memory.Memory;
import fdit.gui.schemaEditor.schemaInterpretation.memory.RangeConstant;
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
import javafx.util.Pair;

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
import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;
import static java.lang.String.valueOf;
import static org.apache.commons.collections4.IterableUtils.first;
import static org.apache.commons.collections4.IterableUtils.get;

class SchemaSwitchInterpretation extends AttackScenarioSwitch<Object> {

    private final Memory memory;
    private final LTLConditionFacade filterFacade = LTLConditionFacade.get();
    private final TriggeringConditionFacade triggerFacade = TriggeringConditionFacade.get();
    private final Collection<Aircraft> targetedAircrafts = newArrayList();
    private final AttackScenarioDslFacade attackScenarioDslFacade;
    private Schema schema;
    private String currentBinaryOp = "";
    private TimeInterval timeInterval;

    SchemaSwitchInterpretation(final AttackScenarioDslFacade attackScenarioDslFacade)
            throws NoSuchAlgorithmException {
        this.attackScenarioDslFacade = attackScenarioDslFacade;
        filterFacade.initialize(FDIT_MANAGER.getRoot());
        triggerFacade.initialize(FDIT_MANAGER.getRoot());
        memory = new Memory();
    }

    public AlterationSpecification processInterpretation(final Schema schema) {
        memory.clear();
        this.schema = schema;
        timeInterval = new TimeInterval(0, schema.getRecording().getMaxRelativeDate());
        try {
            attackScenarioDslFacade.parse(schema.getContent());
        } catch (final Exception e) {
            throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
        return (AlterationSpecification) doSwitch(get(attackScenarioDslFacade.getAST(), 0));
    }

    @Override
    public Object caseASTScenario(final ASTScenario object) {
        final Collection<Action> actions = newArrayList();
        targetedAircrafts.clear();
        object.getDeclarations().forEach(astDeclaration ->
                memory.addConstant((Constant) doSwitch(astDeclaration)));
        object.getInstructions().forEach(astInstruction -> {
            actions.addAll((Collection<Action>) doSwitch(astInstruction));
        });
        final AlterationSchema alterationSchema = new AlterationSchema(
                "Scenario_0",
                "Scenario_0",
                actions);
        final AlterationSpecification alterationSpecification =
                new AlterationSpecification(schema.getName(), alterationSchema);
        alterationSpecification.setFather(schema.getFather());
        return alterationSpecification;
    }

    @Override
    public Object caseASTList(final ASTList object) {
        throw new RuntimeException();
    }

    @Override
    public Object caseASTRange(final ASTRange object) {
        return super.caseASTRange(object);
    }

    @Override
    public Object caseASTDeclaration(final ASTDeclaration object) {
        throw new RuntimeException();
    }

    @Override
    public Object caseASTInstruction(final ASTInstruction object) {
        throw new RuntimeException();
    }

    @Override
    public Object caseASTTarget(final ASTTarget object) {
        throw new RuntimeException();
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
        return new AircraftWayPoint(new Coordinates((double) doSwitch(object.getLatitude()),
                (double) doSwitch(object.getLongitude())),
                (int) doSwitch(object.getAltitude()),
                (long) doSwitch(object.getTime()));
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
    public Object caseASTTime(final ASTTime object) {
        return ((Integer) doSwitch(object.getRealTime())).longValue() * 1000;
    }

    @Override
    public Object caseASTBinaryOp(final ASTBinaryOp object) {
        throw new RuntimeException();
    }

    @Override
    public Object caseASTValue(final ASTValue object) {
        throw new RuntimeException();
    }

    @Override
    public Object caseASTIntegerRange(final ASTIntegerRange object) {
        return new Pair<>(object.getStart(), object.getEnd());
    }

    @Override
    public Object caseASTDoubleRange(final ASTDoubleRange object) {
        return new Pair<>(object.getStart(), object.getEnd());
    }

    @Override
    public Object caseASTStringList(final ASTStringList object) {
        final List<String> values = newArrayList();
        values.addAll(object.getItems());
        return values;
    }

    @Override
    public Object caseASTIntegerList(final ASTIntegerList object) {
        final List<Integer> values = newArrayList();
        values.addAll(object.getItems());
        return values;
    }

    @Override
    public Object caseASTDoubleList(final ASTDoubleList object) {
        final List<Double> values = newArrayList();
        values.addAll(object.getItems());
        return values;
    }

    @Override
    public Object caseASTListDeclaration(final ASTListDeclaration object) {
        return new ListConstant(object.getConstant(), (List) doSwitch(object.getList()));
    }

    @Override
    public Object caseASTRangeDeclaration(final ASTRangeDeclaration object) {
        final Pair value = (Pair) doSwitch(object.getRange());
        return new RangeConstant(object.getConstant(), value.getKey(), value.getValue());
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
        final Collection<Action> actions = newArrayList();
        targetedAircrafts.addAll((Collection<Aircraft>) doSwitch(object.getTarget()));
        final Scope scope = (Scope) doSwitch(object.getTimeScope());
        actions.add(
                new Delay(
                        "Delay_0",
                        "Description_0",
                        renderAircraftIds(targetedAircrafts),
                        scope,
                        (Timestamp) doSwitch(object.getDelay())));
        return actions;
    }

    @Override
    public Object caseASTDelayParameter(final ASTDelayParameter object) {
        final long delay = (long) doSwitch(object.getDelay());
        return new Timestamp(delay);
    }

    @Override
    public Object caseASTAlter(final ASTAlter object) {
        final Collection<Action> actions = newArrayList();
        targetedAircrafts.addAll((Collection<Aircraft>) doSwitch(object.getTarget()));
        final Scope scope = (Scope) doSwitch(object.getTimeScope());
        final Collection<AlterationParameter> parameters = (Collection<AlterationParameter>) doSwitch(object.getParameters());
        if (object.getTrigger() == null) {
            actions.add(
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
                    actions.add(
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
        return actions;
    }

    @Override
    public Object caseASTHide(final ASTHide object) {
        final Collection<Action> actions = newArrayList();
        targetedAircrafts.addAll((Collection<Aircraft>) doSwitch(object.getTarget()));
        final Scope scope = (Scope) doSwitch(object.getTimeScope());
        final Frequency frequency;
        if (object.getParameters() == null) {
            frequency = new Frequency(1);
        } else {
            final Collection<ActionParameter> parameters = (Collection<ActionParameter>) doSwitch(object.getParameters());
            frequency = tryFind(parameters, Frequency.class).orElse(new Frequency(1));
        }
        if (object.getTrigger() == null) {
            actions.add(
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
                    actions.add(
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
        return actions;
    }

    @Override
    public Object caseASTSaturate(final ASTSaturate object) {
        final Collection<Action> actions = newArrayList();
        targetedAircrafts.addAll((Collection<Aircraft>) doSwitch(object.getTarget()));
        final Scope scope = (Scope) doSwitch(object.getTimeScope());
        final Collection<ActionParameter> parameters = (Collection<ActionParameter>) doSwitch(object.getParameters());
        final AircraftNumber aircraftNumber = find(parameters, AircraftNumber.class);
        final AlterationParameter icaoParameter = find(parameters, AlterationParameter.class);
        if (object.getTrigger() == null) {
            actions.add(
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
                    actions.add(
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
        return actions;
    }

    @Override
    public Object caseASTCreate(final ASTCreate object) {
        final Collection<Action> actions = newArrayList();
        final Scope scope = (Scope) doSwitch(object.getTimeScope());
        if (scope instanceof TimeWindow) {
            final Trajectory trajectory = new Trajectory((Collection<AircraftWayPoint>) doSwitch(object.getTrajectory()));
            final Collection<AlterationParameter> parameters = (Collection<AlterationParameter>) doSwitch(object.getParameters());
            actions.add(
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
        return actions;
    }

    @Override
    public Object caseASTReplay(final ASTReplay object) {
        memory.setReplayAttack(true);
        final Collection<Action> actions = newArrayList();
        final Collection<Aircraft> aircrafts = (Collection<Aircraft>) doSwitch(object.getTarget());
        final Collection<AlterationParameter> parameters = (Collection<AlterationParameter>) doSwitch(object.getParameters());
        targetedAircrafts.addAll(aircrafts);
        final Scope scope = (Scope) doSwitch(object.getTimeScope());
        actions.add(
                new Replay(
                        "Replay_0",
                        "Description_0",
                        renderAircraftIds(targetedAircrafts),
                        scope,
                        new RecordName(memory.getTargetedRecording().getName()),
                        parameters));
        return actions;
    }


    @Override
    public Object caseASTTrajectory(final ASTTrajectory object) {
        final Collection<Action> actions = newArrayList();
        targetedAircrafts.addAll((Collection<Aircraft>) doSwitch(object.getTarget()));
        final Scope scope = (Scope) doSwitch(object.getTimeScope());
        final Trajectory trajectory = new Trajectory((Collection<AircraftWayPoint>) doSwitch(object.getTrajectory()));
        if (object.getTrigger() == null) {
            actions.add(
                    new TrajectoryModification(
                            "Alteration_0",
                            "Description_0",
                            renderAircraftIds(targetedAircrafts),
                            scope,
                            trajectory));
        } else {
            ((HashMap<Aircraft, Collection<TimeInterval>>) doSwitch(object.getTrigger())).forEach((key, value) -> {
                int i = 0;
                for (final TimeInterval interval : value) {
                    actions.add(
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
        return actions;
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
    public Object caseASTEqual(final ASTEqual object) {
        if (object.getValue() instanceof ASTIntegerValue) {
            return parseDouble(currentBinaryOp) == (Integer) doSwitch(object.getValue()) * 1.0;
        }

        if (object.getValue() instanceof ASTDoubleValue) {
            return Double.valueOf(currentBinaryOp).equals(doSwitch(object.getValue()));
        }

        if (object.getValue() instanceof ASTStringValue) {
            return currentBinaryOp.toLowerCase().equals(((String) doSwitch(object.getValue())).toLowerCase());
        }
        throw new RuntimeException();
    }

    @Override
    public Object caseASTDifferent(final ASTDifferent object) {
        if (object.getValue() instanceof ASTIntegerValue) {
            return parseInt(currentBinaryOp) != (int) doSwitch(object.getValue());
        }

        if (object.getValue() instanceof ASTDoubleValue) {
            return !Double.valueOf(currentBinaryOp).equals(doSwitch(object.getValue()));
        }

        if (object.getValue() instanceof ASTStringValue) {
            return currentBinaryOp.compareToIgnoreCase((String) doSwitch(object.getValue())) != 0;
        }
        if (object.getValue() instanceof ASTConstantValue) {
            final Constant constant = (Constant) doSwitch(object.getValue());
            if (constant instanceof ListConstant) {
                boolean result = true;
                for (final Object value : ((ListConstant) constant).getValues()) {
                    result &= !currentBinaryOp.equalsIgnoreCase(value.toString());
                }
                return result;
            }
            if (constant instanceof RangeConstant) {
                return parseDouble(currentBinaryOp) < parseDouble(valueOf(((RangeConstant) constant).getStart())) &&
                        parseDouble(currentBinaryOp) > parseDouble(valueOf(((RangeConstant) constant).getEnd()));
            }
        }
        throw new RuntimeException();
    }

    @Override
    public Object caseASTLt(final ASTLt object) {
        if (object.getValue() instanceof ASTIntegerValue) {
            return parseDouble(currentBinaryOp) < (Integer) doSwitch(object.getValue()) * 1.0;
        }

        if (object.getValue() instanceof ASTDoubleValue) {
            return Double.valueOf(currentBinaryOp) < (float) doSwitch(object.getValue());
        }
        throw new RuntimeException();
    }

    @Override
    public Object caseASTGt(final ASTGt object) {

        if (object.getValue() instanceof ASTIntegerValue) {
            return parseDouble(currentBinaryOp) > (Integer) doSwitch(object.getValue()) * 1.0;
        }

        if (object.getValue() instanceof ASTDoubleValue) {
            return Double.valueOf(currentBinaryOp) > (float) doSwitch(object.getValue());
        }
        throw new RuntimeException();
    }

    @Override
    public Object caseASTLte(final ASTLte object) {
        if (object.getValue() instanceof ASTIntegerValue) {
            return parseDouble(currentBinaryOp) <= (Integer) doSwitch(object.getValue()) * 1.0;
        }

        if (object.getValue() instanceof ASTDoubleValue) {
            return Double.valueOf(currentBinaryOp) <= (float) doSwitch(object.getValue());
        }
        throw new RuntimeException();
    }

    @Override
    public Object caseASTGte(final ASTGte object) {
        if (object.getValue() instanceof ASTIntegerValue) {
            return parseDouble(currentBinaryOp) >= (Integer) doSwitch(object.getValue()) * 1.0;
        }

        if (object.getValue() instanceof ASTDoubleValue) {
            return Double.valueOf(currentBinaryOp) >= (float) doSwitch(object.getValue());
        }
        throw new RuntimeException();
    }

    @Override
    public Object caseASTIn(final ASTIn object) {
        if (object.getValue() instanceof ASTConstantValue) {
            final Constant constant = (Constant) doSwitch(object.getValue());
            if (constant instanceof ListConstant) {
                boolean result = false;
                for (final Object value : ((ListConstant) constant).getValues()) {
                    result |= currentBinaryOp.equalsIgnoreCase(value.toString());
                }
                return result;
            }
            if (constant instanceof RangeConstant) {
                return parseDouble(currentBinaryOp) >= parseDouble(valueOf(((RangeConstant) constant).getStart())) &&
                        parseDouble(currentBinaryOp) <= parseDouble(valueOf(((RangeConstant) constant).getEnd()));
            }
        }
        throw new RuntimeException();
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
    public Object caseASTVariableValue(final ASTVariableValue object) {
        throw new RuntimeException();
    }

    @Override
    public Object caseASTConstantValue(final ASTConstantValue object) {
        return memory.getConstant(object.getContent());
    }
}